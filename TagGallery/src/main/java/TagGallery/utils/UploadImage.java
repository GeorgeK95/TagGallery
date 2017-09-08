package TagGallery.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;
import TagGallery.bindingModel.AlbumBindingModel;
import TagGallery.entity.Album;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UploadImage {
    public static String UploadImageFile(UploadImageType type, MultipartFile file) {
        if (type == UploadImageType.Album) {
            return upload(System.getProperty("user.dir") + Paths.ALBUM_IMAGE_DIRECTORY, Paths.ALBUM_FOLDER_PATH, Paths.IMG_ALBUM_WIDTH, Paths.IMG_ALBUM_HEIGHT, Paths.ALBUM_IMAGE_STR, file);
        } else if (type == UploadImageType.Profile) {
            return upload(System.getProperty("user.dir") + Paths.PROFILE_IMAGE_DIRECTORY, Paths.PROFILE_FOLDER_PATH, Paths.IMG_PROFILE_WIDTH, Paths.IMG_PROFILE_HEIGHT, Paths.PROFILE_IMAGE_STR, file);
        } else {
            return null;
        }
    }

    public static List<String> uploadProfileImages(MultipartFile file, String imageName) {
        File imageFile = getImageFile(file, Paths.PROFILE_IMAGE_DIRECTORY);

        String img = doUploadAndDelete(System.getProperty("user.dir") + Paths.PROFILE_IMAGE_DIRECTORY, Paths.PROFILE_FOLDER_PATH,
                Paths.IMG_PROFILE_WIDTH, Paths.IMG_PROFILE_HEIGHT, Paths.PROFILE_IMAGE_STR, false, imageFile);
        String imgSmall = doUploadAndDelete(System.getProperty("user.dir") + Paths.PROFILE_IMAGE_DIRECTORY, Paths.PROFILE_FOLDER_PATH,
                Paths.IMG_PROFILE_SMALL_WIDTH, Paths.IMG_PROFILE_SMALL_HEIGHT, Paths.PROFILE_IMAGE_SMALL_STR, false, imageFile);

        deleteOriginalFile(imageFile);
        return new ArrayList<String>() {{
            add(img);
            add(imgSmall);
        }};
    }

    private static File getImageFile(MultipartFile file, String dir) {
        String directoryParam = System.getProperty("user.dir") + dir;
        File imageFile = null;

        if (file != null) {
            String originalName = file.getOriginalFilename();
            imageFile = new File(directoryParam, originalName);
            try {
                file.transferTo(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imageFile;
    }

    public static List<String> uploadAllSizeImage(MultipartFile file) {
        File imageFile = getImageFile(file, Paths.IMAGE_DIRECTORY);

        String img = doUploadAndDelete(System.getProperty("user.dir") + Paths.IMAGE_DIRECTORY, Paths.IMAGE_FOLDER_PATH,
                Paths.IMG_IMAGE_WIDTH, Paths.IMG_IMAGE_HEIGHT, Paths.IMAGE_STR, false, imageFile);
        String imgBig = doUploadAndDelete(System.getProperty("user.dir") + Paths.IMAGE_DIRECTORY, Paths.IMAGE_FOLDER_PATH,
                Paths.IMG_BIG_WIDTH, Paths.IMG_BIG_HEIGHT, Paths.IMAGE_BIG_STR, false, imageFile);
        String imgSmall = doUploadAndDelete(System.getProperty("user.dir") + Paths.IMAGE_DIRECTORY, Paths.IMAGE_FOLDER_PATH,
                Paths.IMG_SMALL_WIDTH, Paths.IMG_SMALL_HEIGHT, Paths.IMAGE_SMALL_STR, false, imageFile);

        deleteOriginalFile(imageFile);
        return new ArrayList<String>() {{
            add(img);
            add(imgBig);
            add(imgSmall);
        }};
    }


    private static String upload(String imageDirectoryParam, String folderPathParam, int widthParam, int heightParam,
                                 String imageStringParam, MultipartFile file) {
        String path = null;
        if (file != null) {
            String originalName = file.getOriginalFilename();
            File imageFile = new File(imageDirectoryParam, originalName);
            try {
                file.transferTo(imageFile);
                path = doUploadAndDelete(imageDirectoryParam, folderPathParam, widthParam, heightParam,
                        imageStringParam, true, imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    private static String doUploadAndDelete(String imageDirectoryParam, String folderPathParam, int widthParam, int heightParam,
                                            String imageStringParam, boolean deleteOriginalFile, File imageFile) {
        String finalName = resizeAndWriteImage(imageDirectoryParam, imageFile, widthParam, heightParam, imageStringParam);
        if (deleteOriginalFile) {
            deleteOriginalFile(imageFile);
        }
        return folderPathParam + finalName;
    }

    public static void deleteOriginalFile(String filePath) {
        filePath = System.getProperty("user.dir") + filePath;
        File imageFile = new File(filePath);
        if (!imageFile.delete()) {
            System.out.println("Delete operation is failed.");
        }
    }

    public static void deleteOriginalFiles(List<String> filePaths) {
        for (String filePath : filePaths) {
            filePath = System.getProperty("user.dir") + filePath;
            File imageFile = new File(filePath);
            if (!imageFile.delete()) {
                System.out.println("Delete operation is failed.");
            }
        }
    }

    public static void deleteOriginalFile(File imageFile) {
        if (!imageFile.delete()) {
            System.out.println("Delete operation is failed.");
        }
    }

    private static String generateUniqueFileName() {
        String filename = "";
        long millis = System.currentTimeMillis();
        String datetime = new Date().toGMTString();
        datetime = datetime.replace(" ", "");
        datetime = datetime.replace(":", "");
        String rndchars = RandomStringUtils.randomAlphanumeric(16);
        filename = rndchars + "_" + datetime + "_" + millis;
        return filename;
    }

    private static String resizeAndWriteImage(String imageDirectoryParam, File imageFile, int widthParam, int heightParam,
                                              String imageStringParam) {
        BufferedImage originalImage = null;
        String dest = null;
        try {
            originalImage = ImageIO.read(imageFile);
            BufferedImage resizeImagePng = Scalr.resize(originalImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, widthParam, heightParam);
            String name;

            do {
                name = generateUniqueFileName();
            }
            while (new File(name).exists());

            String extension = "";
            String fileName = imageFile.getName();

            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i);
            }

            dest = imageStringParam + name + extension;
            ImageIO.write(resizeImagePng, "png", new File(imageDirectoryParam, dest));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dest;
    }

    public static void uploadAlbumImageDeleteOld(Album album, AlbumBindingModel albumBindingModel) {
        if (!albumBindingModel.getPicture().getOriginalFilename().equals("")) {
            String oldPicturePath = Paths.ALBUM_IMAGE_DIRECTORY + "\\" + album.getAlbumProfileImage().substring(14);
            String path = UploadImage.UploadImageFile(UploadImageType.Album, albumBindingModel.getPicture());
            album.setAlbumProfileImage(path);
            UploadImage.deleteOriginalFile(oldPicturePath);
        }

        album.setTitle(albumBindingModel.getTitle());
    }

}
