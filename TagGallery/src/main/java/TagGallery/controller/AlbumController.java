package TagGallery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import TagGallery.bindingModel.AlbumBindingModel;
import TagGallery.bindingModel.TagBindingModel;
import TagGallery.entity.Album;
import TagGallery.entity.Image;
import TagGallery.entity.Tag;
import TagGallery.entity.User;
import TagGallery.repository.AlbumRepository;
import TagGallery.repository.ImageRepository;
import TagGallery.repository.UserRepository;
import TagGallery.utils.DeleteImage;
import TagGallery.utils.Paths;
import TagGallery.utils.UploadImage;
import TagGallery.utils.UploadImageType;

import java.util.List;
import java.util.Set;

/**
 * Created by George-Lenovo on 6/29/2017.
 */
@Controller
public class AlbumController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private ImageController imageController;

    private static final int ALBUMS_PER_PAGE = 8;
    private static final int IMAGES_PER_PAGE = 12;

    private Long albumCurrentPage = 1L;
    private Long imageCurrentPage = 1L;

    @GetMapping("/albums")
    public String albums(Model model) {
        return this.listAlbums(getUser().getId(), model);
    }

    @GetMapping("/albums/{id}")
    public String listAlbums(@PathVariable Long id, Model model) {
        User user = this.userRepository.findOne(id);
        List<Album> userAlbums = this.albumRepository.getUserAlbums(user);

        List<Album> albumsForPage = userAlbums
                .subList(Math.toIntExact(albumCurrentPage * ALBUMS_PER_PAGE - ALBUMS_PER_PAGE),
                        Math.toIntExact(albumCurrentPage * ALBUMS_PER_PAGE) > userAlbums.size()
                                ? userAlbums.size() : Integer.valueOf(albumCurrentPage.toString()) * ALBUMS_PER_PAGE);

        model.addAttribute("delete_album", Paths.DELETE_IMAGE_PATH);
        model.addAttribute("edit_album", Paths.EDIT_IMAGE_PATH);
        model.addAttribute("albumAddImagePath", Paths.ADD_ALBUM_IMAGE_PATH);
        model.addAttribute("user", user);
        model.addAttribute("albums", albumsForPage);
        model.addAttribute("pageNum", albumCurrentPage);
        model.addAttribute("view", "album/listAlbums");
        Long totalPages = getAlbumTotalPagesCount(userAlbums.size());
        model.addAttribute("maxPage", totalPages);

        albumCurrentPage = 1L;
        return "base-layout";
    }


    @GetMapping("/albums/{userId}/page/{pageNum}")
    public String albumPaging(@PathVariable Long userId, @PathVariable Long pageNum) {
        if (pageNum < 1) {
            pageNum = (long) 1;
        }
        User user = this.userRepository.findOne(userId);
        Long totalPages = getAlbumTotalPagesCount(this.albumRepository.getUserAlbums(user).size());

        if (pageNum > totalPages) {
            pageNum = totalPages;
        }

        albumCurrentPage = pageNum;
        return "redirect:/albums/" + userId;
    }

    @GetMapping("/newAlbum")
    @PreAuthorize("isAuthenticated()")
    public String createAlbumMap(Model model) {
        model.addAttribute("view", "album/newAlbumForm");
        return "base-layout";
    }

    @PostMapping("/newAlbum")
    @PreAuthorize("isAuthenticated()")
    public String createAlbumPost(AlbumBindingModel albumBindingModel, TagBindingModel tagBindingModel) {
        if (!albumBindingModel.getTitle().equals("Default Album")) {
            User user = getUser();
            MultipartFile file = albumBindingModel.getPicture();
            String path = UploadImage.UploadImageFile(UploadImageType.Album, file);
            List<MultipartFile> files = albumBindingModel.getPictures();
            Album album = new Album(albumBindingModel.getTitle(), path, user);
            this.albumRepository.saveAndFlush(album);
            user.addAlbum(album);
            Set<Tag> tags = TagController.getTagsFromTagBindingModel(tagBindingModel.getTags());
            addImagesToAlbum(files, album, tags);
        }
        return "redirect:/albums";
    }


    @GetMapping("/album/{id}")
    public String albumDetailsMap(Model model, @PathVariable Long id) {
        if (!this.albumRepository.exists(id)) {
            return "redirect:/albums";
        }

        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
            User entityUser = getUser();
            model.addAttribute("user", entityUser);
        }

        Album album = this.albumRepository.findOne(id);
        List<Image> images = album.getAlbumImages();
        List<Image> imagesForPage = images
                .subList(Math.toIntExact(imageCurrentPage * IMAGES_PER_PAGE - IMAGES_PER_PAGE),
                        Math.toIntExact(imageCurrentPage * IMAGES_PER_PAGE) > images.size()
                                ? images.size() : Integer.valueOf(imageCurrentPage.toString()) * IMAGES_PER_PAGE);

        model.addAttribute("delete_image", Paths.DELETE_IMAGE_PATH);
        model.addAttribute("albumAddImagePath", Paths.ADD_ALBUM_IMAGE_PATH);
        model.addAttribute("album", album);
        model.addAttribute("images", imagesForPage);
        model.addAttribute("view", "album/albumDetails");
        model.addAttribute("pageNum", imageCurrentPage);

        Long totalPages = getImageTotalPagesCount(images.size());
        model.addAttribute("maxPage", totalPages);
        imageCurrentPage = 1L;

        return "base-layout";
    }


    @GetMapping("/album/{albumId}/page/{pageNum}")
    public String imagePaging(@PathVariable Long albumId, @PathVariable Long pageNum) {
        if (pageNum < 1) {
            pageNum = (long) 1;
        }
        Long totalPages = getImageTotalPagesCount(this.albumRepository.findOne(albumId).getAlbumImages().size());

        if (pageNum > totalPages) {
            pageNum = totalPages;
        }

        imageCurrentPage = pageNum;
        return "redirect:/album/" + albumId;
    }


    @GetMapping("/album/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editMap(@PathVariable Long id, Model model) {
        if (id == 1) {
            return "redirect:/albums";
        }
        if (!this.albumRepository.exists(id)) {
            return "redirect:/albums";
        }

        Album album = this.albumRepository.findOne(id);

        if (!isUserAuthorOrAdmin(album)) {
            return "redirect:/albums";
        }

        model.addAttribute("album", album);
        model.addAttribute("user", album.getAuthor());
        model.addAttribute("view", "album/edit");

        return "base-layout";
    }

    @PostMapping("/album/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editPost(@PathVariable Long id, AlbumBindingModel albumBindingModel) {
        if (!this.albumRepository.exists(id)) {
            return "redirect:/albums";
        }

        Album album = this.albumRepository.findOne(id);

        if (!isUserAuthorOrAdmin(album)) {
            return "redirect:/albums";
        }

        UploadImage.uploadAlbumImageDeleteOld(album, albumBindingModel);

        this.albumRepository.saveAndFlush(album);
        return "redirect:/album/" + id;
    }

    @GetMapping("/album/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteAlbum(Model model, @PathVariable Long id) {
        if (id == 1) {
            return "redirect:/albums";
        }
        if (!this.albumRepository.exists(id)) {
            return "redirect:/albums";
        }

        Album album = this.albumRepository.findOne(id);

        if (!isUserAuthorOrAdmin(album)) {
            return "redirect:/album/" + id;
        }

        model.addAttribute("album", album);
        model.addAttribute("view", "album/delete");

        return "base-layout";
    }

    @PostMapping("/album/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteAlbumProcess(@PathVariable Long id) {
        if (!this.albumRepository.exists(id)) {
            return "redirect:/albums";
        }

        Album album = this.albumRepository.findOne(id);
        DeleteImage.deleteAlbumFiles(album);

        this.albumRepository.delete(album);
        return "redirect:/albums";
    }

    private Long getImageTotalPagesCount(int countOfElements) {
        return (long) (Math.ceil(countOfElements / Double.parseDouble(String.valueOf(IMAGES_PER_PAGE))));
    }

    private boolean isUserAuthorOrAdmin(Album album) {
        User userEntity = getUser();
        return userEntity.isAdmin() || userEntity.isAuthor(album);
    }

    private Long getAlbumTotalPagesCount(int countOfElements) {
        return (long) (Math.ceil(countOfElements / Double.parseDouble(String.valueOf(ALBUMS_PER_PAGE))));
    }

    private User getUser() {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return this.userRepository.findByEmail(principal.getUsername());
    }

    private void addImagesToAlbum(List<MultipartFile> files, Album album, Set<Tag> tags) {
        for (MultipartFile file : files) {
            Image image = this.imageController.uploadImage(file, album, tags);
            if (image != null) {
                this.imageRepository.saveAndFlush(image);
            }
        }
    }
}
