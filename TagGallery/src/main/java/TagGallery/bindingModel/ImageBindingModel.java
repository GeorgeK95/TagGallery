package TagGallery.bindingModel;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by George-Lenovo on 6/28/2017.
 */
public class ImageBindingModel {
    private MultipartFile picture;
    private Long albumId;

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public MultipartFile getPicture() {
        return picture;
    }

    public void setPicture(MultipartFile picture) {
        this.picture = picture;
    }
}
