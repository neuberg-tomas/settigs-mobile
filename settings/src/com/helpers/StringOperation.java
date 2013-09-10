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

/**
 * operace s cisly
 * 
 * @author error414
 *
 */
public class StringOperation {

	/**
	 * cislo na procenta
	 * 
	 * odstraneni diakritiky
	 * 
	 * @param base
	 * @param num
	 * @return
	 */
	final static public String removeSpecialChars(String text)
	{
		return text
				.replace('ě', 'e')
				.replace('š', 's')
				.replace('č', 'c')
				.replace('ř', 'r')
				.replace('ž', 'z')
				.replace('ý', 'y')
				.replace('á', 'a')
				.replace('í', 'i')
				.replace('é', 'e')
				.replace('ú', 'u')
				.replace('ů', 'u');
	}
}