package com.dev.flashback_v04;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import java.util.ArrayList;
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
	private static final Map<Pattern, Integer> textstyling = new HashMap<Pattern, Integer>();
	private static final ArrayList<Pattern> linking = new ArrayList<Pattern>();

	static {
        addEmoticonPattern(emoticons, "[IMAGESMILE]", R.drawable.smile1);
        addEmoticonPattern(emoticons, "[IMAGEYES]", R.drawable.yes);
        addEmoticonPattern(emoticons, "[IMAGEGRIN]", R.drawable.grin);
        addEmoticonPattern(emoticons, "[IMAGENOEXPRESSION]", R.drawable.noexpression);
        addEmoticonPattern(emoticons, "[IMAGESAD]", R.drawable.sad);
        addEmoticonPattern(emoticons, "[IMAGEWHOCO5]", R.drawable.whoco5);
        addEmoticonPattern(emoticons, "[IMAGESKAMSEN]", R.drawable.skamsen);
        addEmoticonPattern(emoticons, "[IMAGEEVILGRIN39]", R.drawable.evilgrin39);
        addEmoticonPattern(emoticons, "[IMAGESNEAKY]", R.drawable.sneaky);
        addEmoticonPattern(emoticons, "[IMAGEWHISTLE]", R.drawable.whistle);
        addEmoticonPattern(emoticons, "[IMAGEEEK!]", R.drawable.w00t);
        addEmoticonPattern(emoticons, "[IMAGEINNOCENT]", R.drawable.innocent);
        addEmoticonPattern(emoticons, "[IMAGEANGRY]", R.drawable.angry);
        addEmoticonPattern(emoticons, "[IMAGEDEVIL]", R.drawable.devil);
        addEmoticonPattern(emoticons, "[IMAGEEVILMAD]", R.drawable.evilmad);
        addEmoticonPattern(emoticons, "[IMAGERANT]", R.drawable.rant);
        addEmoticonPattern(emoticons, "[IMAGETHUMBSUP]", R.drawable.thumbsup);
        addEmoticonPattern(emoticons, "[IMAGEW00T]", R.drawable.w00t);
        addEmoticonPattern(emoticons, "[IMAGEW000T]", R.drawable.w00t);
        addEmoticonPattern(emoticons, "[IMAGECRY]", R.drawable.cry);
        addEmoticonPattern(emoticons, "[IMAGEWHINK]", R.drawable.wink);
        addEmoticonPattern(emoticons, "[IMAGEOHMY]", R.drawable.ohmy);
        addEmoticonPattern(emoticons, "[IMAGETONGUE]", R.drawable.tongue);
        addEmoticonPattern(emoticons, "[IMAGESAD44]", R.drawable.sad44);
        addEmoticonPattern(emoticons, "[IMAGECOOL]", R.drawable.cool2);
        addEmoticonPattern(emoticons, "[IMAGEROLLEYES]", R.drawable.rolleyes);
        addEmoticonPattern(emoticons, "[IMAGELAUGH]", R.drawable.laugh);
        addEmoticonPattern(emoticons, "[IMAGEUNSURE]", R.drawable.unsure);
        addEmoticonPattern(emoticons, "[IMAGECONFUSED]", R.drawable.confused);
        addEmoticonPattern(emoticons, "[IMAGEBOXING]", R.drawable.boxing);
        addEmoticonPattern(emoticons, "[IMAGEDRUNK]", R.drawable.drunk);
        addEmoticonPattern(emoticons, "[IMAGENO]", R.drawable.no);
        addEmoticonPattern(emoticons, "[IMAGESLY]", R.drawable.sly);
        addEmoticonPattern(emoticons, "[IMAGEBEER]", R.drawable.beer2);

		addStylingPattern(textstyling, "\\[b\\](.+?)\\[/b\\]", Typeface.BOLD);
		addStylingPattern(textstyling, "\\[i\\](.+?)\\[/i\\]", Typeface.ITALIC);
		addStylingPattern(textstyling, "\\[u\\](.+?)\\[/u\\]", -1);

		addLinkPattern(linking, "https?://[^/\\s]+/\\S+\\.(jpg|png|gif)");
    }

	private static void addLinkPattern(ArrayList<Pattern> list, String pattern) {
		list.add(Pattern.compile(pattern));
	}

    private static void addEmoticonPattern(Map<Pattern, Integer> map, String smile, int resource) {
        map.put(Pattern.compile(Pattern.quote(smile)), resource);
    }

	private static void addStylingPattern(Map<Pattern, Integer> map, String style, int resource) {
		map.put(Pattern.compile(style, Pattern.DOTALL), resource);
	}

    private static boolean addSmiles(Context context, Spannable spannable) {
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

	private static Spannable addStyling(Spannable spannable) {
		SpannableStringBuilder builtstring = new SpannableStringBuilder(spannable);

		for (Map.Entry<Pattern, Integer> entry : textstyling.entrySet()) {
			Matcher matcher = entry.getKey().matcher(builtstring);
			while (matcher.find()) {
				if(entry.getValue() != -1) {
					builtstring.setSpan(new StyleSpan(entry.getValue()),
							matcher.start(1), matcher.end(1),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else {
					builtstring.setSpan(new UnderlineSpan(),
							matcher.start(1), matcher.end(1),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				builtstring.delete(matcher.end()-4, matcher.end());
				builtstring.delete(matcher.start(), matcher.start()+3);
				matcher = entry.getKey().matcher(builtstring);
			}
		}
		return builtstring;
	}

	private static Spannable addInternalLinks(Spannable spannable, Context context) {
		SpannableStringBuilder builtstring = new SpannableStringBuilder(spannable);

		for (Pattern entry : linking) {
			Matcher matcher = entry.matcher(builtstring);
			while (matcher.find()) {
				boolean set = true;
				for (InternalLink span : spannable.getSpans(matcher.start(), matcher.end(), InternalLink.class))
					if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end()) {
						spannable.removeSpan(span);
					} else {
						set = false;
						break;
					}
				if (set) {
					String url = matcher.group(0);
					InternalLink link = new InternalLink(context, url);
					String replacement = "Bildlänk";
					builtstring.delete(matcher.start(), matcher.end());
					builtstring.insert(matcher.start(), replacement);
					builtstring.setSpan(new InternalLink(context, url),
							matcher.start(), matcher.start() + replacement.length(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					matcher = entry.matcher(builtstring);
				}
			}
		}
		return builtstring;
	}

    public static Spannable getStyledText(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable);
		spannable = addStyling(spannable);
		spannable = addInternalLinks(spannable, context);

        return spannable;
    }
}
