package TagGallery.utils;

import TagGallery.entity.Album;
import TagGallery.entity.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by George-Lenovo on 6/29/2017.
 */
public class DeleteImage {
    public static void deleteAlbumFiles(Album album) {
        if (!album.getTitle().equals(Paths.DEFAULT_ALBUM_TITLE)) {
            String albumProfileImage = Paths.ALBUM_IMAGE_DIRECTORY + "\\" + album.getAlbumProfileImage().substring(14);
            UploadImage.deleteOriginalFile(albumProfileImage);
        }
        UploadImage.deleteOriginalFiles(album.getAlbumImages().stream().map(x -> Paths.IMAGE_DIRECTORY + "\\" + x.getImagePath().substring(14)).collect(Collectors.toList()));
        UploadImage.deleteOriginalFiles(album.getAlbumImages().stream().map(x -> Paths.IMAGE_DIRECTORY + "\\" + x.getBigImagePath().substring(14)).collect(Collectors.toList()));
        UploadImage.deleteOriginalFiles(album.getAlbumImages().stream().map(x -> Paths.IMAGE_DIRECTORY + "\\" + x.getSmallImagePath().substring(14)).collect(Collectors.toList()));
    }

    public static void deleteImageFiles(List<String> originalNamesAndFolders) {
        try {
            for (String current : originalNamesAndFolders) {
                String originalName = current.substring(14);
                File imageFile = new File(System.getProperty("user.dir") + Paths.IMAGE_DIRECTORY + "\\" + originalName);
                if (imageFile.delete()) {
                    System.out.println(imageFile.getName() + " is deleted!");
                } else {
                    System.out.println("Delete operation is failed!");
                }
            }
        } catch (Exception ex) {
            System.out.println("Failed to delete image!");
        }
    }

    public static void deleteImagesFiles(Set<Image> images) {
        for (Image image : images) {
            DeleteImage.deleteImageFiles(
                    new ArrayList<String>() {{
                        add(image.getImagePath());
                        add(image.getBigImagePath());
                        add(image.getSmallImagePath());
                    }}
            );
        }
    }
}
