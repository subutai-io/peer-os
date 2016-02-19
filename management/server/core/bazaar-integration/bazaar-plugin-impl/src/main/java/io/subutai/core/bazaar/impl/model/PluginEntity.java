package io.subutai.core.bazaar.impl.model;

import io.subutai.core.bazaar.api.model.Plugin;

import javax.persistence.*;

@Entity
@Table ( name = "hub_plugin" )
@Access ( AccessType.FIELD )
public class PluginEntity implements Plugin
{
	@Id
	@Column( name = "id" )
	@GeneratedValue( strategy = GenerationType.IDENTITY )
	private Long id;

	@Column( name = "name" )
	private String name;

	@Column( name = "version" )
	private String version;

	@Column( name = "kar" )
	private String kar;

	@Column (name = "url")
	private String url;

	public Long getId()
	{
		return id;
	}


	public void setId( final Long id )
	{
		this.id = id;
	}


	public String getName()
	{
		return name;
	}


	public void setName( final String name )
	{
		this.name = name;
	}


	public String getKar()
	{
		return kar;
	}


	public void setKar( final String kar )
	{
		this.kar = kar;
	}


	public String getVersion()
	{
		return version;
	}


	public void setVersion( final String version )
	{
		this.version = version;
	}

	@Override
	public String getUrl ()
	{
		return this.url;
	}

	@Override
	public void setUrl (String url)
	{
		this.url = url;
	}
}
