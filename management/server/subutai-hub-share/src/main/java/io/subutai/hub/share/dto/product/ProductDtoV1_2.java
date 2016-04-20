package io.subutai.hub.share.dto.product;


import java.text.ParseException;

import org.json.JSONObject;


/**
 * ProductDto for Hub REST of version 1.2
 */
public class ProductDtoV1_2 extends ProductDto
{
    private String iconUrl;


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
