package TagGallery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import TagGallery.bindingModel.TagBindingModel;
import TagGallery.entity.Image;
import TagGallery.entity.Tag;
import TagGallery.repository.ImageRepository;
import TagGallery.utils.Paths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by George-Lenovo on 6/29/2017.
 */
@Controller
public class TagSearchController {
    @Autowired
    private ImageRepository imageRepository;

    private Long searchTagCurrentPage = 1L;
    private int totalPages = 0;
    private static final int IMAGES_PER_PAGE = 60;
    private List<Image> allImages;

    @GetMapping("/list")
    public String search(TagBindingModel tagBindingModel, Model model) {
        if (tagBindingModel.getTags() != null) {
            allImages = getTagImages(tagBindingModel);
        }
        this.totalPages = allImages.size();

        List<Image> imagesForPage = allImages
                .subList(Math.toIntExact(searchTagCurrentPage * IMAGES_PER_PAGE - IMAGES_PER_PAGE),
                        Math.toIntExact(searchTagCurrentPage * IMAGES_PER_PAGE) > allImages.size()
                                ? allImages.size() : Integer.valueOf(searchTagCurrentPage.toString()) * IMAGES_PER_PAGE);

        model.addAttribute("delete_image", Paths.DELETE_IMAGE_PATH);
        model.addAttribute("albumAddImagePath", Paths.ADD_ALBUM_IMAGE_PATH);
        model.addAttribute("images", imagesForPage);
        model.addAttribute("view", "tag/indexFromSearch");
        model.addAttribute("pageNum", searchTagCurrentPage);
        model.addAttribute("totalImages", allImages.size());

        Long totalPages = getImageTotalPagesCount(allImages.size());
        model.addAttribute("maxPage", totalPages);
        searchTagCurrentPage = 1L;

        return "base-layout";
    }

    private Long getImageTotalPagesCount(int countOfElements) {
        return (long) (Math.ceil(countOfElements / Double.parseDouble(String.valueOf(IMAGES_PER_PAGE))));
    }

    @GetMapping("/list/page/{pageNum}")
    public String tagSearchPaging(@PathVariable Long pageNum) {
        if (pageNum < 1) {
            pageNum = (long) 1;
        }

        Long totalPages = getImageTotalPagesCount(this.totalPages);

        if (pageNum > totalPages) {
            pageNum = totalPages;
        }

        this.searchTagCurrentPage = pageNum;
        return "redirect:/list";
    }

    private List<Image> getTagImages(TagBindingModel tagBindingModel) {
        Set<String> tags = TagController.getTagsFromTagBindingModel(tagBindingModel.getTags()).stream()
                .map(Tag::getTagName).collect(Collectors.toSet());
        return getImagesByTags(tags);
    }

    private List<Image> getImagesByTags(Set<String> tags) {
        Set<Long> ids = new HashSet<>();

        for (String tag : tags) {
            Set<Image> images = this.imageRepository.findImagesByTag(tag);
            ids.addAll(images.stream().map(Image::getId).collect(Collectors.toSet()));
        }

        if (ids.size() > 0) {
            return this.imageRepository.findByIdsOrderedByAddDate(ids);
        }

        return new ArrayList<>();
    }
}
