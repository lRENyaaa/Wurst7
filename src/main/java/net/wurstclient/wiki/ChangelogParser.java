/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.wiki;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.wurstclient.update.Version;

public enum ChangelogParser
{
	;
	
	private static final TreeMap<String, List<String>> changelogs =
		new TreeMap<>(Comparator.comparing(Version::new));
	
	private static final Pattern versionRegex =
		Pattern.compile("wurst-version: \"(.+)\"");
	
	public static void parseFolder(Path folder)
	{
		try(Stream<Path> stream = Files.walk(folder))
		{
			stream.filter(Files::isRegularFile)
				.forEach(ChangelogParser::parseFile);
			
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static void parseFile(Path path)
	{
		List<String> lines;
		
		try
		{
			lines = Files.readAllLines(path);
			
		}catch(IOException e)
		{
			new IOException("Coudln't read " + path, e).printStackTrace();
			return;
		}
		
		String version = null;
		int endOfFrontmatter = 0;
		
		// read metadata
		for(int i = 1; i < lines.size(); i++)
		{
			String line = lines.get(i);
			Matcher matcher = versionRegex.matcher(line);
			
			if(version == null && matcher.find())
			{
				version = matcher.group(1);
				continue;
			}
			
			if(line.equals("---"))
			{
				endOfFrontmatter = i;
				break;
			}
		}
		
		if(version == null)
			throw new IllegalStateException(
				"Couldn't parse version of " + path);
		
		if(endOfFrontmatter == 0)
			throw new IllegalStateException(
				"Couldn't parse frontmatter of " + path);
		
		Stream<String> stream =
			lines.subList(endOfFrontmatter + 1, lines.size()).stream();
		
		// feature lists
		stream = stream.filter(change -> !change.contains("<li>"));
		
		// keybinds
		stream = stream.filter(change -> !change.contains("->"));
		
		// ClickGUI spam
		stream = stream.filter(change -> !change.contains(
			"-based ClickGUI (which you can open by pressing **Right CTRL**)."));
		stream = stream.filter(change -> !change
			.contains("Window-based ClickGUI (press **Right CTRL** to open)."));
		stream = stream.filter(change -> !change.contains(
			"**Note:** If you can't open the ClickGUI by pressing Right CTRL, try using the following command (in the Minecraft chat): <code>.binds&nbsp;reset</code>"));
		stream = stream.filter(
			change -> !change.contains("Some usable hacks are ClickGUI,"));
		
		// TabGUI spam
		stream = stream.filter(change -> !change
			.contains("TabGUI (must be enabled through Navigator"));
		stream = stream.filter(change -> !change
			.contains("TabGUI will be added back in later pre-releases."));
		
		// .help spam
		stream = stream.filter(
			change -> !change.contains("All keybinds can be changed in-game."));
		
		stream = stream.map(change -> {
			if(change.startsWith("- "))
				return change.substring(2);
			return change;
		});
		
		// fix wiki links
		stream = stream.map(change -> change.replaceAll(
			"\\[(.+?)\\]\\(https://wiki\\.wurstclient\\.net/.+?\\)", "[[$1]]"));
		
		// fix code/commands formatting
		stream = stream.map(change -> change.replace("`", "''"));
		
		// fix "Thanks to ...!" entries
		stream = stream.map(change -> change.replaceAll(
			"\\(Thanks to <a href=\"https://github.com/[^\"]+\"[^>]+>([^<]+)</a>!\\)",
			"(Thanks to [[gh>$1]]!)"));
		
		// fix extra whitespace
		stream = stream.map(String::trim);
		
		ArrayList<String> changes =
			stream.collect(Collectors.toCollection(ArrayList::new));
		
		// fix "... is back!" entries
		if(version.startsWith("7.0pre"))
			for(int i = 0; i < changes.size(); i++)
			{
				String change = changes.get(i);
				if(!change.endsWith(" is back!"))
					continue;
				
				String feature =
					change.substring(0, change.indexOf(" is back!"));
				
				ArrayList<String> changes2 = new ArrayList<>();
				
				List<String> oldChanges2 = changelogs.get("7.0pre1");
				if(oldChanges2 != null)
					changes2.addAll(oldChanges2);
				
				changes2.add("Temporarily removed " + feature + ".");
				
				changelogs.put("7.0pre1",
					Collections.unmodifiableList(changes2));
				
				changes.set(i, "Re-added " + feature + ".");
			}
		
		changelogs.put(version, Collections.unmodifiableList(changes));
	}
	
	public static Map<String, List<String>> getChangelogs()
	{
		return Collections.unmodifiableMap(changelogs);
	}
}
