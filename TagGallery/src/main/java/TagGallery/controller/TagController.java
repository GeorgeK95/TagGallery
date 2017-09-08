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
import TagGallery.repository.TagRepository;
import TagGallery.utils.Paths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class TagController {

    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private TagRepository tagRepository;

    private static final int IMAGES_PER_PAGE = 60;
    private Long tagCurrentPage = 1L;

    @GetMapping("/list/{id}")
    public String searchById(@PathVariable Long id, Model model) {
        TagBindingModel tagBindingModel = new TagBindingModel();
        Tag tag = this.tagRepository.findOne(id);
        tagBindingModel.setTags(tag.getTagName());

        List<Image> tagImages = getTagImages(tagBindingModel);
        List<Image> imagesForPage = tagImages
                .subList(Math.toIntExact(this.tagCurrentPage * IMAGES_PER_PAGE - IMAGES_PER_PAGE),
                        Math.toIntExact(this.tagCurrentPage * IMAGES_PER_PAGE) > tagImages.size()
                                ? tagImages.size() : Integer.valueOf(this.tagCurrentPage.toString()) * IMAGES_PER_PAGE);

        model.addAttribute("view", "tag/index");
        model.addAttribute("delete_image", Paths.DELETE_IMAGE_PATH);
        model.addAttribute("images", imagesForPage);
        model.addAttribute("tag", tag);
        model.addAttribute("pageNum", tagCurrentPage);
        Long totalPages = getImageTotalPagesCount(tagImages.size());
        model.addAttribute("maxPage", totalPages);
        model.addAttribute("totalImages", tagImages.size());

        tagCurrentPage = 1L;
        return "base-layout";
    }

    @GetMapping("/list/{tagId}/page/{pageNum}")
    public String tagPaging(@PathVariable Long tagId, @PathVariable Long pageNum) {
        if (pageNum < 1) {
            pageNum = (long) 1;
        }
        Long totalPages = getImageTotalPagesCount(this.tagRepository.findOne(tagId).getImages().size());

        if (pageNum > totalPages) {
            pageNum = totalPages;
        }

        this.tagCurrentPage = pageNum;
        return "redirect:/list/" + tagId;
    }

    private Long getImageTotalPagesCount(int countOfElements) {
        return (long) (Math.ceil(countOfElements / Double.parseDouble(String.valueOf(IMAGES_PER_PAGE))));
    }


    private List<Image> getTagImages(TagBindingModel tagBindingModel) {
        Set<String> tags = getTagsFromTagBindingModel(tagBindingModel.getTags()).stream()
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

    static Set<Tag> getTagsFromTagBindingModel(String tagString) {
        tagString = tagString.replace("\\s+", "");
        String[] split = tagString.split("#+");
        Set<String> tags = new HashSet<>();

        for (String s : split) {
            if (!s.equals("")) {
                tags.add(s);
            }
        }

        return tags.stream().map(Tag::new).collect(Collectors.toSet());
    }
}
