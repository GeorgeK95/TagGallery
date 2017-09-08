package TagGallery.bindingModel;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

public class AlbumBindingModel {
    @NotNull
    private String title;
    @NotNull
    private MultipartFile picture;


    private List<MultipartFile> pictures;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPictures(List<MultipartFile> pictures) {
        this.pictures = pictures;
    }

    public List<MultipartFile> getPictures() {
        return this.pictures;
    }

    public MultipartFile getPicture() {
        return this.picture;
    }

    public void setPicture(MultipartFile picture) {
        this.picture = picture;
    }
}
