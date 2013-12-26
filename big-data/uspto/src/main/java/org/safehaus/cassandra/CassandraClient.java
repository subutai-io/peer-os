package org.safehaus.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraClient {

	private Cluster cluster;
	private Session session;

	public CassandraClient() {
		// TODO Auto-generated constructor stub
	}

	public void connect(String node) {

		// If already contacted to a cluster, first close it
		if (cluster != null) {
			close();
		}

		cluster = Cluster.builder().addContactPoint(node)
		// .withSSL() // Uncomment if using client to node encryption
				.build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n",
				metadata.getClusterName());
		for (Host host : metadata.getAllHosts()) {
			System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		}

		session = cluster.connect();
	}

	public void close() {
		if (cluster != null) {
			cluster.shutdown();
		}
	}

	public void createSchema() {
		session.execute("CREATE KEYSPACE uspto WITH replication "
				+ "= {'class':'SimpleStrategy', 'replication_factor':1};");
		
		session.execute("CREATE TABLE uspto.xml (" + "id text PRIMARY KEY,"
				+ "date text,"
				+ "applId text,"
				+ "applDate text,"
				+ "xml text,"
				+ "json text"
				+ ");");
//		session.execute("CREATE TABLE simplex.playlists (" + "id uuid,"
//				+ "title text," + "album text, " + "artist text,"
//				+ "song_id uuid," + "PRIMARY KEY (id, title, album, artist)"
//				+ ");");
	}

	public void loadData() {
		session.execute("INSERT INTO simplex.songs (id, title, album, artist, tags) "
				+ "VALUES ("
				+ "756716f7-2e54-4715-9f00-91dcbea6cf50,"
				+ "'La Petite Tonkinoise',"
				+ "'Bye Bye Blackbird',"
				+ "'Joséphine Baker'," + "{'jazz', '2013'})" + ";");
		session.execute("INSERT INTO simplex.playlists (id, song_id, title, album, artist) "
				+ "VALUES ("
				+ "2cc9ccb7-6221-4ccb-8387-f22b6a1b354d,"
				+ "756716f7-2e54-4715-9f00-91dcbea6cf50,"
				+ "'La Petite Tonkinoise',"
				+ "'Bye Bye Blackbird',"
				+ "'Joséphine Baker'" + ");");
	}
	
	public void insertXML(String id, String date, String applId, String applDate, String xmlString, String jsonString)
	{
		session.execute("INSERT INTO uspto.xml (id, date, applId, applDate, xml, json)"
				+ "VALUES ('"+id+"', "
						+ "'"+date+"', "
						+ "'"+applId+"', "
						+ "'"+applDate+"', "
						+ "'"+xmlString+"', "
						+ "'"+jsonString+"');");
	}

	public String selectXML(String id)
	{	
		ResultSet results = session.execute("SELECT * FROM uspto.xml WHERE id = '"+id+"';");
		for (Row row : results)
		{
			return row.getString("xml");
		}
		return "";
	}
	
	public void queryData() {
		ResultSet results = session.execute("SELECT * FROM simplex.playlists "
				+ "WHERE id = 2cc9ccb7-6221-4ccb-8387-f22b6a1b354d;");

		System.out
				.println(String
						.format("%-30s\t%-20s\t%-20s\n%s", "title", "album",
								"artist",
								"-------------------------------+-----------------------+--------------------"));
		for (Row row : results) {
			System.out.println(String.format("%-30s\t%-20s\t%-20s",
					row.getString("title"), row.getString("album"),
					row.getString("artist")));
		}
		System.out.println();
	}

	public void dropSchema(String keyspace) {
		getSession().execute("DROP KEYSPACE " + keyspace);
		System.out.println("Finished dropping " + keyspace + " keyspace.");
	}

	public Session getSession() {
		return this.session;
	}
}
