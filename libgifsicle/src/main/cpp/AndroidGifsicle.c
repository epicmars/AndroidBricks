//
// Created on 2019-11-08.
//
#include "AndroidGifsicle.h"
#include "gifsicle.h"
#include "kcolor.h"
#include <jni.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <android/log.h>
#include <android/bitmap.h>

Gt_Frame def_frame;

Gt_Frameset *frames = 0;
int first_input_frame = 0;
Gt_Frameset *nested_frames = 0;

Gif_Stream *input = 0;
const char *input_name = 0;
static int unoptimizing = 0;

const int GIFSICLE_DEFAULT_THREAD_COUNT = 8;
int thread_count = 0;

static int gif_read_flags = 0;
static int nextfile = 0;
Gif_CompressInfo gif_write_info;

static int frames_done = 0;
static int files_given = 0;

int warn_local_colormaps = 1;

static Gt_ColorTransform *input_transforms;
static Gt_ColorTransform *output_transforms;

int mode = BLANK_MODE;
int nested_mode = 0;

static int infoing = 0;
int verbosing = 0;
static int no_ignore_errors = 0;

/* frame option types */
static int next_frame = 0;
#define CH_INTERLACE            0
#define CH_DISPOSAL             1
#define CH_DELAY                2
#define CH_TRANSPARENT          3
#define CH_COMMENT              4
#define CH_NAME                 5
#define CH_POSITION             6
#define CH_CROP                 7
#define CH_EXTENSION            8
#define CH_FLIP                 9
#define CH_ROTATE               10
static const char *frame_option_types[] = {
        "interlace", "disposal", "delay", "transparency",
        "comment", "name", "position", "crop",
        "extension", "flip", "rotation"
};

/* input option types */
static int next_input = 0;
#define CH_UNOPTIMIZE           0
#define CH_CHANGE_COLOR         1
static const char *input_option_types[] = {
        "unoptimization", "color change"
};

/* output option types */
static Gt_OutputData def_output_data;
Gt_OutputData active_output_data;
static int next_output = 0;
static int active_next_output = 0;
static int any_output_successful = 0;

Clp_Parser* clp;

static void do_colormap_change(Gif_Stream *gfs);
static void initialize_def_frame(void);
static void output_information(const char *outfile);
static void
write_stream(const char *output_name, Gif_Stream *gfs);

JNIEXPORT void JNICALL Java_com_androidpi_bricks_libgifsicle_Gifsicle_gifInfo(JNIEnv *env, jobject this, jstring gif) {
    const char *gifPath = (*env)->GetStringUTFChars(env, gif, 0);
    init();
    input_stream(gifPath);
    /* Obtain a C-copy of the Java string */
    char *logPath = "/sdcard/Download/info.txt";
    output_information(logPath);
    (*env)->ReleaseStringUTFChars(env, gif, gifPath);
}

JNIEXPORT void JNICALL Java_com_androidpi_bricks_libgifsicle_Gifsicle_gifImages(JNIEnv *env, jobject this, jstring gif, jstring workspace) {
    const char *gifPath = (*env)->GetStringUTFChars(env, gif, 0);
    const char *workspacePath = (*env)->GetStringUTFChars(env, workspace, 0);
    init();
    input_stream(gifPath);
    explode(workspacePath);
    (*env)->ReleaseStringUTFChars(env, gif, gifPath);
    (*env)->ReleaseStringUTFChars(env, workspace, workspacePath);
}


static void
output_information(const char *outfile)
{
    FILE *f;
    int i, j;
    Gt_Frame *fr;
    Gif_Stream *gfs;

    f = fopen(outfile, "w");
    if (!f) {
        lerror(outfile, "%s", strerror(errno));
        return;
    }

    for (i = 0; i < frames->count; i++)
        FRAME(frames, i).stream->user_flags = 97;

    for (i = 0; i < frames->count; i++)
        if (FRAME(frames, i).stream->user_flags == 97) {
            fr = &FRAME(frames, i);
            gfs = fr->stream;
            gfs->user_flags = 0;
            stream_info(f, gfs, fr->input_filename, fr->info_flags);
            for (j = i; j < frames->count; j++)
                if (FRAME(frames, j).stream == gfs) {
                    fr = &FRAME(frames, j);
                    image_info(f, gfs, fr->image, fr->info_flags);
                }
        }

    if (f != stderr && f != stdout)
        fclose(f);
}

static void
merge_and_write_frames(const char *outfile, int f1, int f2)
{
    Gif_Stream *out;
    int compress_immediately;
    int colormap_change;
    int huge_stream;
    assert(!nested_mode);
    if (verbosing)
        verbose_open('[', outfile ? outfile : "#stdout#");
    active_output_data.active_output_name = outfile;

    colormap_change = active_output_data.colormap_size > 0
                      || active_output_data.colormap_fixed;
    warn_local_colormaps = !colormap_change;

    if (!(active_output_data.scaling
          || (active_output_data.optimizing & GT_OPT_MASK)
          || colormap_change))
        compress_immediately = 1;
    else
        compress_immediately = active_output_data.conserve_memory;

    out = merge_frame_interval(frames, f1, f2, &active_output_data,
                               compress_immediately, &huge_stream);

    if (out) {
        double w, h;
        if (active_output_data.scaling == GT_SCALING_SCALE) {
            w = active_output_data.scale_x * out->screen_width;
            h = active_output_data.scale_y * out->screen_height;
        } else {
            w = active_output_data.resize_width;
            h = active_output_data.resize_height;
        }
        if (active_output_data.scaling != GT_SCALING_NONE)
            resize_stream(out, w, h, active_output_data.resize_flags,
                          active_output_data.scale_method,
                          active_output_data.scale_colors);
        if (colormap_change)
            do_colormap_change(out);
        if (output_transforms)
            apply_color_transforms(output_transforms, out);
        if (active_output_data.optimizing & GT_OPT_MASK)
            optimize_fragments(out, active_output_data.optimizing, huge_stream);
        write_stream(outfile, out);
        Gif_DeleteStream(out);
    }

    if (verbosing)
        verbose_close(']');
    active_output_data.active_output_name = 0;
}

static void
do_colormap_change(Gif_Stream *gfs)
{
    if (active_output_data.colormap_fixed || active_output_data.colormap_size > 0)
        kc_set_gamma(active_output_data.colormap_gamma_type,
                     active_output_data.colormap_gamma);

    if (active_output_data.colormap_fixed)
        colormap_stream(gfs, active_output_data.colormap_fixed,
                        &active_output_data);

    if (active_output_data.colormap_size > 0) {
        kchist kch;
        Gif_Colormap* (*adapt_func)(kchist*, Gt_OutputData*);
        Gif_Colormap *new_cm;

        /* set up the histogram */
        {
            uint32_t ntransp;
            int i, any_locals = 0;
            for (i = 0; i < gfs->nimages; i++)
                if (gfs->images[i]->local)
                    any_locals = 1;
            kchist_make(&kch, gfs, &ntransp);
            if (kch.n <= active_output_data.colormap_size
                && !any_locals
                && !active_output_data.colormap_fixed) {
                warning(1, "trivial adaptive palette (only %d colors in source)", kch.n);
                kchist_cleanup(&kch);
                return;
            }
            active_output_data.colormap_needs_transparency = ntransp > 0;
        }

        switch (active_output_data.colormap_algorithm) {
            case COLORMAP_DIVERSITY:
                adapt_func = &colormap_flat_diversity;
                break;
            case COLORMAP_BLEND_DIVERSITY:
                adapt_func = &colormap_blend_diversity;
                break;
            case COLORMAP_MEDIAN_CUT:
                adapt_func = &colormap_median_cut;
                break;
            default:
                fatal_error("can't happen");
        }

        new_cm = (*adapt_func)(&kch, &active_output_data);
        colormap_stream(gfs, new_cm, &active_output_data);

        Gif_DeleteColormap(new_cm);
        kchist_cleanup(&kch);
    }
}

/*****
 * output GIF images
 **/

static void
write_stream(const char *output_name, Gif_Stream *gfs)
{
    FILE *f;

    if (output_name)
        f = fopen(output_name, "wb");

    if (f) {
        Gif_FullWriteFile(gfs, &gif_write_info, f);
        fclose(f);
    } else
        LOGE("%s : %s", output_name, strerror(errno));
}

static void
initialize_def_frame(void)
{
    /* frame defaults */
    def_frame.stream = 0;
    def_frame.image = 0;
    def_frame.use = 1;

    def_frame.name = 0;
    def_frame.no_name = 0;
    def_frame.comment = 0;
    def_frame.no_comments = 0;

    def_frame.interlacing = -1;
    def_frame.transparent.haspixel = 0;
    def_frame.left = -1;
    def_frame.top = -1;
    def_frame.position_is_offset = 0;

    def_frame.crop = 0;

    def_frame.delay = -1;
    def_frame.disposal = -1;

    def_frame.nest = 0;
    def_frame.explode_by_name = 0;

    def_frame.no_extensions = 0;
    def_frame.no_app_extensions = 0;
    def_frame.extensions = 0;

    def_frame.flip_horizontal = 0;
    def_frame.flip_vertical = 0;
    def_frame.total_crop = 0;

    /* output defaults */
    def_output_data.output_name = 0;

    def_output_data.screen_width = -1;
    def_output_data.screen_height = -1;
    def_output_data.background.haspixel = 0;
    def_output_data.loopcount = -2;

    def_output_data.colormap_size = 0;
    def_output_data.colormap_fixed = 0;
    def_output_data.colormap_algorithm = COLORMAP_DIVERSITY;
    def_output_data.dither_type = dither_none;
    def_output_data.dither_name = "none";
    def_output_data.colormap_gamma_type = KC_GAMMA_SRGB;
    def_output_data.colormap_gamma = 2.2;

    def_output_data.optimizing = 0;
    def_output_data.scaling = GT_SCALING_NONE;
    def_output_data.scale_method = SCALE_METHOD_MIX;
    def_output_data.scale_colors = 0;

    def_output_data.conserve_memory = 0;

    active_output_data = def_output_data;
}

static void
gifread_error(Gif_Stream* gfs, Gif_Image* gfi,
              int is_error, const char* message)
{
    static int last_is_error = 0;
    static char last_landmark[256];
    static char last_message[256];
    static int different_error_count = 0;
    static int same_error_count = 0;
    char landmark[256];
    int which_image = Gif_ImageNumber(gfs, gfi);
    if (gfs && which_image < 0)
        which_image = gfs->nimages;

    /* ignore warnings if "no_warning" */
    if (no_warnings && is_error == 0)
        return;

    if (message) {
        const char *filename = gfs && gfs->landmark ? gfs->landmark : "<unknown>";
        if (gfi && (which_image != 0 || gfs->nimages != 1))
            snprintf(landmark, sizeof(landmark), "%s:#%d",
                     filename, which_image < 0 ? gfs->nimages : which_image);
        else
            snprintf(landmark, sizeof(landmark), "%s", filename);
    }

    if (last_message[0]
        && different_error_count <= 10
        && (!message
            || strcmp(message, last_message) != 0
            || strcmp(landmark, last_landmark) != 0)) {
        const char* etype = last_is_error ? "read error: " : "";
        void (*f)(const char*, const char*, ...) = last_is_error ? lerror : lwarning;
        if (gfi && gfi->user_flags)
            /* error already reported */;
        else if (same_error_count == 1)
            f(last_landmark, "%s%s", etype, last_message);
        else if (same_error_count > 0)
            f(last_landmark, "%s%s (%d times)", etype, last_message, same_error_count);
        same_error_count = 0;
        last_message[0] = 0;
    }

    if (message) {
        if (last_message[0] == 0)
            different_error_count++;
        same_error_count++;
        strncpy(last_message, message, sizeof(last_message));
        last_message[sizeof(last_message) - 1] = 0;
        strncpy(last_landmark, landmark, sizeof(last_landmark));
        last_landmark[sizeof(last_landmark) - 1] = 0;
        last_is_error = is_error;
        if (different_error_count == 11) {
            if (!(gfi && gfi->user_flags))
                error(0, "(plus more errors; is this GIF corrupt?)");
            different_error_count++;
        }
    } else
        last_message[0] = 0;

    {
        unsigned long missing;
        if (message && sscanf(message, "missing %lu pixel", &missing) == 1
            && missing > 10000 && no_ignore_errors) {
            gifread_error(gfs, 0, -1, 0);
            lerror(landmark, "fatal error: too many missing pixels, giving up");
            exit(1);
        }
    }

    if (gfi && is_error < 0)
        gfi->user_flags |= 1;
}

struct StoredFile {
    FILE *f;
    struct StoredFile *next;
    char name[1];
};

static struct StoredFile *stored_files = 0;

static FILE *
open_giffile(const char *name)
{
    struct StoredFile *sf;
    FILE *f;

    if (nextfile)
        for (sf = stored_files; sf; sf = sf->next)
            if (strcmp(name, sf->name) == 0)
                return sf->f;

    f = fopen(name, "rb");

    if (f && nextfile) {
        sf = (struct StoredFile *) malloc(sizeof(struct StoredFile) + strlen(name));
        sf->f = f;
        sf->next = stored_files;
        stored_files = sf;
        strcpy(sf->name, name);
    } else if (!f)
        lerror(name, "%s", strerror(errno));

    return f;
}

static void
close_giffile(FILE *f, int final)
{
    struct StoredFile **sf_pprev, *sf;

    if (!final && nextfile) {
        int c = getc(f);
        if (c == EOF)
            final = 1;
        else
            ungetc(c, f);
    }

    for (sf_pprev = &stored_files; (sf = *sf_pprev); sf_pprev = &sf->next)
        if (sf->f == f) {
            if (final) {
                fclose(f);
                *sf_pprev = sf->next;
                free((void *) sf);
            }
            return;
        }

    if (f != stdin)
        fclose(f);
}

void init() {
    frames = new_frameset(16);
    initialize_def_frame();
    Gif_InitCompressInfo(&gif_write_info);
    Gif_SetErrorHandler(gifread_error);
}

void input_stream(const char *name) {

    FILE *f;
    Gif_Stream *gfs;
    int componentno = 0;
    Gt_Frame old_def_frame;

    f = open_giffile(name);
    if (!f)
        return;

    /* read file */
    {
        int old_error_count = error_count;
        gfs = Gif_FullReadFile(f, gif_read_flags | GIF_READ_COMPRESSED,
                               name, gifread_error);
        if ((!gfs || (Gif_ImageCount(gfs) == 0 && gfs->errors > 0))
            && componentno != 1)
            lerror(name, "trailing garbage ignored");
        if (!no_ignore_errors)
            error_count = old_error_count;
    }

    if (!gfs || (Gif_ImageCount(gfs) == 0 && gfs->errors > 0)) {
        if (componentno == 1)
            lerror(name, "file not in GIF format");
        Gif_DeleteStream(gfs);
        if (verbosing)
            verbose_close('>');
        goto error;
    }

    def_frame.input_filename = input_name;

    old_def_frame = def_frame;
    first_input_frame = frames->count;
    def_frame.position_is_offset = 1;
    int i;
    for (i = 0; i < gfs->nimages; i++)
        add_frame(frames, gfs, gfs->images[i]);
    def_frame = old_def_frame;

    error:
        close_giffile(f, 1);
}

void explode(const char *workspace) {
    /* Use the current output name for consistency, even though that means
    we can't explode different frames to different names. Not a big deal
    anyway; they can always repeat the gif on the cmd line. */
    int i;
    int max_nimages = 0;
    for (i = 0; i < frames->count; i++) {
        Gt_Frame *fr = &FRAME(frames, i);
        if (fr->stream->nimages > max_nimages)
            max_nimages = fr->stream->nimages;
    }

    char *outfile = Gif_NewArray(char, strlen(workspace) + 3);
    sprintf(outfile, "%s/%s", workspace, "-");

    for (i = 0; i < frames->count; i++) {
        Gt_Frame *fr = &FRAME(frames, i);
        int imagenumber = Gif_ImageNumber(fr->stream, fr->image);
        char *explodename;

        const char *imagename = 0;
        if (fr->explode_by_name)
            imagename = fr->name ? fr->name : fr->image->identifier;

        explodename = explode_filename(outfile, imagenumber, imagename,
                                       max_nimages);
        merge_and_write_frames(explodename, i, i);
    }
    Gif_DeleteArray(outfile);
}





