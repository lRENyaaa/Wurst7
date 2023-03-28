/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.wiki;

import java.util.ArrayList;
import java.util.Collection;

import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.command.Command;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.ColorUtils;

public final class WikiPage
{
	private final Feature feature;
	private String text = "";
	private final ArrayList<String> tags = new ArrayList<>();
	
	public WikiPage(Feature feature)
	{
		this.feature = feature;
		
		text += String.format("====== %s ======\n\n", feature.getName());
		text += new HackTable(feature);
		
		String type = feature instanceof Hack ? "Minecraft hack"
			: feature instanceof Command ? "[[:command|chat command]]"
				: "Wurst feature";
		text += String.format("%s is a %s that... FIXME\n\n", feature.getName(),
			type);
		
		if(feature instanceof Command)
			addSyntax();
		
		addSettings();
		
		text += "===== Changes =====\n\n";
		text += new ChangelogTable(feature);
		
		addTags();
	}
	
	private void addSyntax()
	{
		text += "===== Syntax =====\n\n";
		text += "Main article: [[Command Syntax]]\n\n";
		
		Command cmd = (Command)feature;
		
		for(String syntax : cmd.getSyntax())
		{
			if(syntax.startsWith("Syntax: "))
				syntax = syntax.substring(8);
			
			text += "  * ''" + syntax
				+ "'' FIXME explain what this does.\\\\ \\\\ \n";
		}
		
		text += "\n";
		text += "Examples:\n";
		text += "  * ''" + cmd.getName() + "'' FIXME.\\\\ \\\\ \n\n";
	}
	
	private void addSettings()
	{
		Collection<Setting> settings = feature.getSettings().values();
		if(settings.isEmpty())
			return;
		
		text += "===== Settings =====\n\n";
		
		for(Setting setting : settings)
			addSetting(setting);
	}
	
	private void addSetting(Setting setting)
	{
		if(setting instanceof AttackSpeedSliderSetting)
		{
			text += "===== =====\n";
			text += "{{page>attack_speed&link&firstseconly}}\n";
			text += "\n";
			tags.add("has_attack_speed");
			return;
		}
		
		text += "==== " + setting.getName() + " ====\n";
		text += "^  " + setting.getName() + "  ^^\n";
		
		String type = setting instanceof CheckboxSetting ? "Checkbox"
			: setting instanceof SliderSetting ? "Slider"
				: setting instanceof EnumSetting ? "Enum"
					: setting instanceof ColorSetting
						? "[[:ColorSetting|Color]]" : "FIXME";
		
		text += "^Type|" + type + "|\n";
		
		String description = convertDescription(setting.getDescription());
		text += "^In-game description|" + description + "|\n";
		
		if(setting instanceof CheckboxSetting checkbox)
		{
			String defaultValue =
				checkbox.isCheckedByDefault() ? "checked" : "not checked";
			text += "^Default value|" + defaultValue + "|\n";
			
		}else if(setting instanceof SliderSetting slider)
		{
			text += "^Default value|" + slider.getDefaultValue() + "|\n";
			text += "^Minimum|" + slider.getMinimum() + "|\n";
			text += "^Maximum|" + slider.getMaximum() + "|\n";
			text += "^Increment|" + slider.getIncrement() + "|\n";
			
		}else if(setting instanceof EnumSetting<?> enumSetting)
		{
			text +=
				"^Default value|" + enumSetting.getDefaultSelected() + "|\n";
			
			Enum<?>[] enumValues = enumSetting.getValues();
			String values = enumValues[0].toString();
			for(int i = 1; i < enumValues.length; i++)
				values += ", " + enumValues[i];
			
			text += "^Possible values|" + values + "|\n";
			
		}else if(setting instanceof ColorSetting colorSetting)
		{
			String defaultColor =
				ColorUtils.toHex(colorSetting.getDefaultColor());
			text += "^Default value|" + defaultColor + "|\n";
		}
		
		if(description.equals("(none)"))
			text += "\nFIXME Describe here what \"" + setting.getName()
				+ "\" does, since it has no in-game description.\n";
		
		text += "\n";
	}
	
	public static String convertDescription(String input)
	{
		if(input.isEmpty())
			return "(none)";
		
		String translated = WurstClient.INSTANCE.translate(input);
		
		return "\"" + translated.replace("\n", "\\\\ ")
			.replaceAll("\u00a7l([^\u00a7]+)\u00a7r", "**$1**") + "\"";
	}
	
	private void addTags()
	{
		if(tags.isEmpty())
			return;
		
		text += "\n";
		text += "{{tag>" + String.join(" ", tags) + "}}\n";
	}
	
	public String getText()
	{
		return text;
	}
}
