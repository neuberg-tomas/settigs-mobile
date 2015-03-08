/*
Copyright (C) Petr Cada and Tomas Jedrzejek
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package com.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public class HelpLinks {

    final public static String defaultLang = "en";

    final public static String baseUrl = "http://spirit-system.com/";

    final public static String docsUrl = "http://docs.google.com/viewer?url=";

    final public static HashMap<String, String> helpPdflinks = new HashMap<String, String>() {
        {
            //link
            put("cz", "dl/manual/spirit-manual-1.1.0_cz.pdf");
            put("en", "dl/manual/spirit-manual-1.1.0_en.pdf");
            //endlink
        }
    };

    /**
     *
     * @param lang
     * @return
     */
    public static String getPdfUrl(String lang){
        if(helpPdflinks.containsKey(lang)){
            return baseUrl + helpPdflinks.get(lang);
        }

        if(helpPdflinks.containsKey(defaultLang)){
            return baseUrl + helpPdflinks.get(defaultLang);
        }

        return "";
    }

    /**
     *
     * @param lang
     * @return
     */
    public static String getDocsPdfUrl(String lang){
        try {
            return docsUrl +  URLEncoder.encode(getPdfUrl(lang), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

}
