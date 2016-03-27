package io.subutai.hub.share.dto.product;


import java.util.ArrayList;
import java.util.List;

//Version 1.1
public class ProductsDto
{
    private List<ProductDto> productsDto = new ArrayList<>();


    public ProductsDto()
    {
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
