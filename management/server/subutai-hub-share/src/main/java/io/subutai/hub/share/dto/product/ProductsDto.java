package io.subutai.hub.share.dto.product;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//Version 1.1
public class ProductsDto
{
    private static final Logger LOG = LoggerFactory.getLogger( ProductsDto.class.getName() );

    private List<ProductDtoV1_2> productsDto = new ArrayList<>();


    public ProductsDto()
    {
    }


    //JSON String to ProductsDto
    public ProductsDto( String prodString )
    {
        JSONObject object = new JSONObject( prodString );
        JSONArray products = object.getJSONArray( "productDtos" );
        for ( int i = 0; i < products.length(); i++ )
        {
            JSONObject product = products.getJSONObject( i );
            try
            {
                ProductDtoV1_2 productDto = new ProductDtoV1_2( product );
                this.productsDto.add( productDto );
            }
            catch ( ParseException e )
            {
                LOG.warn( e.getMessage() );
            }
        }
        Collections.sort( productsDto, new Comparator<ProductDtoV1_2>()
        {
            @Override
            public int compare( final ProductDtoV1_2 o1, final ProductDtoV1_2 o2 )
            {
                return o1.getName().compareTo( o2.getName() );
            }
        } );
    }


    public ProductsDto( final List<ProductDtoV1_2> productsDto )
    {
        this.productsDto = productsDto;
    }


    public void addProductDto( final ProductDtoV1_2 productDto )
    {
        this.productsDto.add( productDto );
    }


    public void removeProductDto( final ProductDtoV1_2 productDto )
    {
        this.productsDto.remove( productDto );
    }


    public List<ProductDtoV1_2> getProductDtos()
    {
        return productsDto;
    }


    public void setProductDtos( final List<ProductDtoV1_2> productDtos )
    {
        this.productsDto = productDtos;
    }
}
