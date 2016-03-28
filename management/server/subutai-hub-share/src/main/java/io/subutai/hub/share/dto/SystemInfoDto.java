package io.subutai.hub.share.dto;


import java.util.ArrayList;
import java.util.List;


public class SystemInfoDto
{
    private PeerInfoDto peerInfo;

    private List<ProductDto> productDtoList = new ArrayList<>();


    public SystemInfoDto()
    {
    }


    public PeerInfoDto getPeerInfo()
    {
        return peerInfo;
    }


    public void setPeerInfo( final PeerInfoDto peerInfo )
    {
        this.peerInfo = peerInfo;
    }


    public List<ProductDto> getProductDtoList()
    {
        return productDtoList;
    }


    public void setProductDtoList( final List<ProductDto> productDtoList )
    {
        this.productDtoList = productDtoList;
    }


    public void addProductDto( final ProductDto productDto )
    {
        this.productDtoList.add( productDto );
    }


    public void removeProductDto( final ProductDto productDto )
    {
        this.productDtoList.remove( productDto );
    }
}
