package TagGallery.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "album")
public class Album {

    private Long id;

    private String title;

    private String albumProfileImage;

    private List<Image> albumImages;

    private User author;

    public Album() {
    }


    public Album(String title, String imagePath, User author) {
        this.title = title;
        this.albumProfileImage = imagePath;
        this.albumImages = new LinkedList<>();
        this.author = author;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    @Column(nullable = false, length = 30)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @OneToMany(mappedBy = "albumHolder", cascade = CascadeType.REMOVE)
    public List<Image> getAlbumImages() {
        return albumImages;
    }

    public void setAlbumImages(List<Image> albumImages) {
        this.albumImages = albumImages;
    }

    @ManyToOne()
    @NotNull
    @JoinColumn(nullable = false, name = "authorId")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @NotNull
    @Column(nullable = false, name = "profileImagePath")
    public String getAlbumProfileImage() {
        return albumProfileImage;
    }

    public void setAlbumProfileImage(String albumProfileImage) {
        this.albumProfileImage = albumProfileImage;
    }

    public void addPictureToAlbum(Image image) {
        if (image != null) {
            this.albumImages.add(image);
        } else {
            throw new NullPointerException("Image is null!");
        }
    }
}
