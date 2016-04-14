package io.subutai.hub.share.dto.product;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


//Version 1.1
public class ProductsDto
{
    private List<ProductDto> productsDto = new ArrayList<>();


    public ProductsDto()
    {
    }


    //JSON String to ProductsDto
    public ProductsDto( String prodString )
    {
        JSONObject object= new JSONObject( prodString );
        JSONArray products = object.getJSONArray( "productDtos" );
        for ( int i = 0; i < products.length(); i++ )
        {
            JSONObject product = products.getJSONObject( i );
            try
            {
                ProductDto productDto = new ProductDto( product );
                this.productsDto.add( productDto );
            }
            catch ( ParseException e )
            {
                e.printStackTrace();
            }
        }
        Collections.sort( productsDto, new Comparator<ProductDto>()
        {
            @Override
            public int compare( final ProductDto o1, final ProductDto o2 )
            {
                return o1.getName().compareTo( o2.getName() );
            }
        });
    }


    public ProductsDto( final List<ProductDto> productsDto )
    {
        this.productsDto = productsDto;
    }


    public void addProductDto( final ProductDto productDto )
    {
        this.productsDto.add( productDto );
    }


    public void removeProductDto( final ProductDto productDto )
    {
        this.productsDto.remove( productDto );
    }


    public List<ProductDto> getProductDtos()
    {
        return productsDto;
    }


    public void setProductDtos( final List<ProductDto> productDtos )
    {
        this.productsDto = productDtos;
    }
}
