package TagGallery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import TagGallery.bindingModel.ImageBindingModel;
import TagGallery.bindingModel.TagBindingModel;
import TagGallery.entity.*;
import TagGallery.repository.*;
import TagGallery.utils.DeleteImage;
import TagGallery.utils.UploadImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by George-Lenovo on 6/28/2017.
 */

@Controller
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CommentRepository commentRepository;

    private static final int COMMENTS_PER_PAGE = 5;
    static Long currentPage = 1L;

    @GetMapping("/image/{id}")
    public String imageDetails(@PathVariable Long id, Model model) {
        Image image = this.imageRepository.findOne(id);

        if (image != null) {
            List<Comment> byImageHolder = this.commentRepository.findByImageHolderOrderByIdAsc(image);
            List<Comment> commentsForImage = byImageHolder
                    .subList(Math.toIntExact(currentPage * COMMENTS_PER_PAGE - COMMENTS_PER_PAGE),
                            Math.toIntExact(currentPage * COMMENTS_PER_PAGE) > byImageHolder.size()
                                    ? byImageHolder.size() : Integer.valueOf(currentPage.toString()) * COMMENTS_PER_PAGE);
            model.addAttribute("view", "image/details");
            model.addAttribute("image", image);
            List<Tag> tagsByImage = this.tagRepository.findTagsByImage(image.getId());
            model.addAttribute("tags", tagsByImage);
            model.addAttribute("comments", commentsForImage);
            model.addAttribute("pageNum", currentPage);
            Long totalPages = (long) (Math.ceil(byImageHolder.size() / Double.parseDouble(String.valueOf(COMMENTS_PER_PAGE))));
            model.addAttribute("maxPage", totalPages);
            currentPage = 1L;
        }

        return "base-layout";
    }


    @GetMapping("/image/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteImageMap(@PathVariable Long id, Model model) {
        if (!this.imageRepository.exists(id)) {
            return "redirect:/albums";
        }

        Image image = this.imageRepository.findOne(id);
        Album album = image.getAlbumHolder();

        if (!isUserAuthorOrAdmin(image.getAlbumHolder())) {
            return "redirect:/albums";
        }

        model.addAttribute("view", "image/delete");
        model.addAttribute("image", image);
        model.addAttribute("album", album);

        return "base-layout";
    }

    @PostMapping("/image/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteImagePost(@PathVariable Long id) {
        if (!this.imageRepository.exists(id)) {
            return "redirect:/albums";
        }

        Image image = this.imageRepository.findOne(id);
        Album album = image.getAlbumHolder();
        List<String> imagePathsForDelete = new ArrayList<String>() {{
            add(image.getImagePath());
            add(image.getBigImagePath());
            add(image.getSmallImagePath());
        }};

        if (!isUserAuthorOrAdmin(album)) {
            return "redirect:/album/" + album.getId();
        }

        DeleteImage.deleteImageFiles(imagePathsForDelete);

        this.imageRepository.delete(image);

        return "redirect:/album/" + album.getId();
    }

    @GetMapping("/album/addImage")
    @PreAuthorize("isAuthenticated()")
    public String addImageMap(Model model) {
        User user = getUser();
        if (user != null) {
            List<Album> albums = albumRepository.getUserAlbums(user);
            model.addAttribute("view", "image/addImage");
            model.addAttribute("albums", albums);
        }

        return "base-layout";
    }

    @PostMapping("/album/addImage/{id}")
    @PreAuthorize("isAuthenticated()")
    public String addImagePost(ImageBindingModel imageBindingModel, TagBindingModel tagBindingModel) {
        Album album = this.albumRepository.findOne(imageBindingModel.getAlbumId());
        MultipartFile img = imageBindingModel.getPicture();
        Image image = this.uploadImage(img, album, TagController.getTagsFromTagBindingModel(tagBindingModel.getTags()));
        this.imageRepository.saveAndFlush(image);

        System.out.println();
        return "redirect:/";
    }

    private User getUser() {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return this.userRepository.findByEmail(principal.getUsername());
    }

    private UserDetails getUserDetails() {
        return (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private boolean isUserAuthorOrAdmin(Album album) {
        UserDetails user = getUserDetails();
        User userEntity = this.userRepository.findByEmail(user.getUsername());
        return userEntity.isAdmin() || userEntity.isAuthor(album);
    }

    public Image uploadImage(MultipartFile currentFile, Album album, Set<Tag> tags) {
        Image image = null;

        if (!currentFile.getOriginalFilename().equals("")) {
            List<String> imagePaths = UploadImage.uploadAllSizeImage(currentFile);
            image = new Image(imagePaths.get(0), album);
            image.setBigImagePath(imagePaths.get(1));
            image.setSmallImagePath(imagePaths.get(2));
            for (Tag tag : tags) {
                Tag byTagName = this.tagRepository.findByTagName(tag.getTagName());
                if (byTagName == null) {
                    image.addTag(tag);
                } else {
                    image.addTag(byTagName);
                }
            }
        }

        return image;
    }
}
