package com.uniscope.demo.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作者：majun
 * 时间：2019/3/12 18:26
 * 说明：从HTML文件中获取URL类
 */
public class ImageSource
{
	private boolean isDuRegex;

	public void setDuRegex(boolean isBaiduRegex) {
		this.isDuRegex = isBaiduRegex;
	}

	// 规则0、1是普通图片规则，2是百度图片规则
	public static String[] regex = { "\\b(http://){1}[^\\s]+?(\\.jpg){1}\\b",
			"<img\\b[^>]*\\bsrc\\b\\s*=\\s*('|\")?([^'\"\n\r\f>]+(\\.jpg)\\b)[^>]*>", "\"objURL\":\"(.*?)\"" };

	// 把图片地址从HTML文件中解析出来
	public ArrayList<String> ParseHtmlToImage(String html, String regex) {
		// 从网页获取Html数据
		if (html != null) {
			ArrayList<String> imgList = new ArrayList<>();
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(html);
			String group;
			if (isDuRegex) {
				while (matcher.find()) {
					group = matcher.group(1);
					if (!TextUtils.isEmpty(group)) {
						imgList.add(group);
					}
				}
			} else {
				while (matcher.find()) {
					group = matcher.group();
					if (!TextUtils.isEmpty(group)) {
						imgList.add(group);
					}
				}
			}
			return imgList;
		}
		return null;
	}

}
