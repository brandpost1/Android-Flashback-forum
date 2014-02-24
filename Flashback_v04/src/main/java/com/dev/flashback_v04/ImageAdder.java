package com.dev.flashback_v04;

import android.content.Context;
import android.text.Spannable;
import android.text.style.ImageSpan;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Viktor on 2014-01-22.
 * From: http://stackoverflow.com/a/4302199
 */
public class ImageAdder {

    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

    static {
        addPattern(emoticons, "[IMAGESMILE]", R.drawable.smile1);
        addPattern(emoticons, "[IMAGEYES]", R.drawable.yes);
        addPattern(emoticons, "[IMAGEGRIN]", R.drawable.grin);
        addPattern(emoticons, "[IMAGENOEXPRESSION]", R.drawable.noexpression);
        addPattern(emoticons, "[IMAGESAD]", R.drawable.sad);
        addPattern(emoticons, "[IMAGEWHOCO5]", R.drawable.whoco5);
        addPattern(emoticons, "[IMAGESKAMSEN]", R.drawable.skamsen);
        addPattern(emoticons, "[IMAGEEVILGRIN39]", R.drawable.evilgrin39);
        addPattern(emoticons, "[IMAGESNEAKY]", R.drawable.sneaky);
        addPattern(emoticons, "[IMAGEWHISTLE]", R.drawable.whistle);
        addPattern(emoticons, "[IMAGEEEK!]", R.drawable.w00t);
        addPattern(emoticons, "[IMAGEINNOCENT]", R.drawable.innocent);
        addPattern(emoticons, "[IMAGEANGRY]", R.drawable.angry);
        addPattern(emoticons, "[IMAGEDEVIL]", R.drawable.devil);
        addPattern(emoticons, "[IMAGEEVILMAD]", R.drawable.evilmad);
        addPattern(emoticons, "[IMAGERANT]", R.drawable.rant);
        addPattern(emoticons, "[IMAGETHUMBSUP]", R.drawable.thumbsup);
        addPattern(emoticons, "[IMAGEW00T]", R.drawable.w00t);
        addPattern(emoticons, "[IMAGEW000T]", R.drawable.w00t);
        addPattern(emoticons, "[IMAGECRY]", R.drawable.cry);
        addPattern(emoticons, "[IMAGEWHINK]", R.drawable.wink);
        addPattern(emoticons, "[IMAGEOHMY]", R.drawable.ohmy);
        addPattern(emoticons, "[IMAGETONGUE]", R.drawable.tongue);
        addPattern(emoticons, "[IMAGESAD44]", R.drawable.sad44);
        addPattern(emoticons, "[IMAGECOOL]", R.drawable.cool2);
        addPattern(emoticons, "[IMAGEROLLEYES]", R.drawable.rolleyes);
        addPattern(emoticons, "[IMAGELAUGH]", R.drawable.laugh);
        addPattern(emoticons, "[IMAGEUNSURE]", R.drawable.unsure);
        addPattern(emoticons, "[IMAGECONFUSED]", R.drawable.confused);
        addPattern(emoticons, "[IMAGEBOXING]", R.drawable.boxing);
        addPattern(emoticons, "[IMAGEDRUNK]", R.drawable.drunk);
        addPattern(emoticons, "[IMAGENO]", R.drawable.no);
        addPattern(emoticons, "[IMAGESLY]", R.drawable.sly);
        addPattern(emoticons, "[IMAGEBEER]", R.drawable.beer2);
    }

    private static void addPattern(Map<Pattern, Integer> map, String smile, int resource) {
        map.put(Pattern.compile(Pattern.quote(smile)), resource);
    }

    public static boolean addSmiles(Context context, Spannable spannable) {
        boolean hasChanges = false;
        for (Map.Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class))
                    if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end()) {
                        spannable.removeSpan(span);
                    } else {
                        set = false;
                        break;
                    }
                if (set) {
                    hasChanges = true;
                    spannable.setSpan(new ImageSpan(context, entry.getValue()),
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return hasChanges;
    }

    public static Spannable getSmiledText(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable);
        return spannable;
    }
}
