/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.wiki;

import net.wurstclient.Feature;
import net.wurstclient.command.Command;
import net.wurstclient.hack.Hack;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.keybinds.KeybindList;

public final class HackTable
{
	private final Feature feature;
	private String text = "";
	
	public HackTable(Feature feature)
	{
		this.feature = feature;
		
		text += "<WRAP 516px>\n";
		
		text += String.format("^  %s  ^^\n", feature.getName());
		
		String picName = feature.getName().toLowerCase();
		if(picName.startsWith("."))
			picName = picName.substring(1);
		text += String.format("|{{ %s.webp?500 |}}||\n", picName);
		
		String typeValue = feature instanceof Hack ? "Hack"
			: feature instanceof Command ? "Command" : "Other Feature";
		text += String.format("^Type|[[:%s]]|\n", typeValue);
		
		String category = feature.getCategory() == null ? "No Category|none"
			: feature.getCategory().getName();
		text += String.format("^Category|[[:%s]]|\n", category);
		
		String description =
			WikiPage.convertDescription(feature.getDescription());
		text += String.format("^In-game description|%s|\n", description);
		
		String keybind = getDefaultKeybind();
		text += String.format(
			"^[[:keybinds#default_keybinds|Default keybind]]|%s|\n", keybind);
		
		text += String.format("^Source code|[[w7src>%s.java]]|\n",
			feature.getClass().getName().replace(".", "/"));
		
		text += "</WRAP>\n\n";
	}
	
	private String getDefaultKeybind()
	{
		String name = feature.getName().toLowerCase().replace(" ", "_");
		if(name.startsWith("."))
			name = name.substring(1);
		
		for(Keybind keybind : KeybindList.DEFAULT_KEYBINDS)
			if(keybind.getCommands().toLowerCase().contains(name))
				return keybind.getKey().replace("key.keyboard.", "")
					.toUpperCase();
			
		return "none";
	}
	
	@Override
	public String toString()
	{
		return text;
	}
}
