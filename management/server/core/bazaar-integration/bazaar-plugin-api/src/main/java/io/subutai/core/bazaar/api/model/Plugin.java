package io.subutai.core.bazaar.api.model;


public interface Plugin
{
	public Long getId();

	public void setId( final Long id );

	public String getName();

	public void setName( final String name );

	public String getKar();

	public void setKar( final String kar );

	public String getVersion();

	public void setVersion( final String version );

	public String getUrl();

	public void setUrl (final String url);

	public String getUid ();

	public void setUid (String uid);
}
