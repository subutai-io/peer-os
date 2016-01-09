package od;

import net.serenitybdd.jbehave.SerenityStories;

public class AcceptanceTestSuite extends SerenityStories {
String directory_stories;
public AcceptanceTestSuite(){
directory_stories="stories/tests_run/*";
findStoriesCalled(directory_stories);
}
}
