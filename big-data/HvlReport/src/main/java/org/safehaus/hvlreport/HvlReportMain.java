package org.safehaus.hvlreport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HvlReportMain {

	public static void main(String str[]) throws FileNotFoundException, IOException {
		QueryJira queryJira = new QueryJira();
		
		List<Pair> completedTasks = new ArrayList<Pair>();
		List<Pair> ongoingTasks = new ArrayList<Pair>();
		List<Pair> plannedTasks = new ArrayList<Pair>();
		List<Pair> problems = new ArrayList<Pair>();
		
		queryJira.queryIssues(new Date(2014-1900, 1, 10), new Date(2014-1900,2,20), new Date(), completedTasks, ongoingTasks);
		
		UpdateDocument updateDocument = new UpdateDocument();

		plannedTasks.add(new Pair("Planlanan satir 1", new Date()));
		plannedTasks.add(new Pair("Planlanan satir 2", new Date()));
		plannedTasks.add(new Pair("Planlanan satir 3", new Date()));
		
		problems.add(new Pair("Problem satir 1"));
		problems.add(new Pair("Problem satir 2"));
		problems.add(new Pair("Problem satir 3"));

		updateDocument.UpdateTables(new Date(), completedTasks, ongoingTasks, plannedTasks, problems);
	}
		
}
