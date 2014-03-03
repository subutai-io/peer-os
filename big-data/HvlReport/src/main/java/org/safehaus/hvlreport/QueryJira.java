package org.safehaus.hvlreport;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

public class QueryJira {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	String jiraServer = "https://jira.safehaus.org";
	
	public QueryJira() {
		// TODO Auto-generated constructor stub
	}
	
	public boolean queryIssues(Date beginDate, Date endDate, Date reportDate, List<Pair> completedTasks, List<Pair> ongoingTasks)
	{
		AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI jiraServerUri;
		try {
			jiraServerUri = new URI("https://jira.safehaus.org");
		} catch (URISyntaxException e) {
			logger.error("Can't access jira server {}", jiraServer, e);
			return false;
		}
        JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "ssenkul1", "12345");
        
        SimpleDateFormat sdfJira = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdfResolution = new SimpleDateFormat("yyyy-MM-dd");
        IssueRestClient issueClient = restClient.getIssueClient();
        
        //Completed Jobs
        String searchJql = "status in (Closed,Resolved) AND resolutiondate >= '"
        		+ sdfJira.format(beginDate) + "' AND resolutiondate <= '"
        		+ sdfJira.format(endDate)+ "'";
        SearchResult searchResult = restClient.getSearchClient().searchJql(searchJql).claim();
		for (BasicIssue issue : searchResult.getIssues()) {
			String issueKey = issue.getKey();
			Promise<Issue> issuePromise = issueClient.getIssue(issueKey);
			try {
				String resolutionDate = issuePromise.get().getFieldByName("Resolved").getValue().toString().substring(0, 10);
				try {
					completedTasks.add(new Pair(issuePromise.get().getSummary(), sdfResolution.parse(resolutionDate)));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				logger.error("Problem accessing jira issue {}", issueKey, e);
				return false;
			} catch (ExecutionException e) {
				logger.error("Problem accessing jira issue {}", issueKey, e);
				return false;
			}
		}
		logger.info("Completed Jobs finished");
		
        //Ongoing Jobs
        searchJql = "status in (\"In Progress\") AND duedate >= '"
        		+ sdfJira.format(endDate) + "'";
        searchResult = restClient.getSearchClient().searchJql(searchJql).claim();
		for (BasicIssue issue : searchResult.getIssues()) {
			String issueKey = issue.getKey();
			Promise<Issue> issuePromise = issueClient.getIssue(issueKey);
			try {
				ongoingTasks.add(new Pair(issuePromise.get().getSummary(), issuePromise.get().getDueDate().toDate()));
			} catch (InterruptedException e) {
				logger.error("Problem accessing jira issue {}", issueKey, e);
				return false;
			} catch (ExecutionException e) {
				logger.error("Problem accessing jira issue {}", issueKey, e);
				return false;
			}
		}
		
		logger.info("Ongoing Jobs finished");
		
		return true;
	}

}
