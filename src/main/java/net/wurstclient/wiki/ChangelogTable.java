/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.wiki;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.wurstclient.Feature;

public final class ChangelogTable
{
	private final Feature feature;
	private String text = "";
	
	public ChangelogTable(Feature feature)
	{
		this.feature = feature;
		text += "^Version^Changes^\n";
		addChanges();
	}
	
	private void addChanges()
	{
		Set<Entry<String, List<String>>> updates =
			ChangelogParser.getChangelogs().entrySet();
		
		for(Entry<String, List<String>> update : updates)
		{
			String version = update.getKey().replace("-BETA", " Beta");
			List<String> changelog = update.getValue();
			
			addChangesFromUpdate(version, changelog);
		}
	}
	
	private void addChangesFromUpdate(String version, List<String> changelog)
	{
		String featureName = feature.getName();
		boolean firstChangeInVersion = true;
		
		for(String change : changelog)
		{
			if(!change.contains(featureName))
				continue;
			
			if(firstChangeInVersion)
				addTableRow(getUpdateLink(version), change);
			else
				addTableRow(":::", change);
			
			firstChangeInVersion = false;
		}
	}
	
	private String getUpdateLink(String version)
	{
		return "[[update:Wurst " + version + "]]";
	}
	
	private void addTableRow(String column1, String column2)
	{
		text += "|" + column1 + "|" + column2 + "|\n";
	}
	
	@Override
	public String toString()
	{
		return text;
	}
}
