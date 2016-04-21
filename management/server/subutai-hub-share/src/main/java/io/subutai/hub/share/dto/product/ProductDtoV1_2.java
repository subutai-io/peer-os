package io.subutai.hub.share.dto.product;


import java.text.ParseException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ProductDto for Hub REST of version 1.2
 */
public class ProductDtoV1_2 extends ProductDto
{
    private String iconUrl;
    private static final Logger LOG = LoggerFactory.getLogger( ProductDtoV1_2.class );

    public ProductDtoV1_2()
    {
        super();
    }

    //JSONObject to ProductDtoV1_2
    public ProductDtoV1_2( JSONObject objProduct ) throws ParseException
    {
        super( objProduct );
        this.iconUrl = objProduct.getString( "iconUrl" );
    }


    public String getIconUrl()
    {
        return iconUrl;
    }


    public void setIconUrl( final String iconUrl )
    {
        this.iconUrl = iconUrl;
    }
}
