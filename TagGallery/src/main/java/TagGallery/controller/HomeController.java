package TagGallery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import TagGallery.entity.Image;
import TagGallery.entity.Tag;
import TagGallery.repository.TagRepository;
import TagGallery.utils.RandomNumber;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/")
    public String index(Model model) {
        List<Tag> topTags = getTags();
        model.addAttribute("view", "home/index");
        model.addAttribute("tags", topTags);
        return "base-layout";
    }

    private List<Tag> getTags() {
        List<BigInteger> topTagsIds = this.tagRepository.getTopTags();
        List<Tag> tags = new ArrayList<>();
        for (BigInteger topTagsId : topTagsIds) {
            Long currentId = topTagsId.longValue();
            Tag currentTag = this.tagRepository.findOne(currentId);
            currentTag.setRandomImage(generateRandomImage(currentTag));
            tags.add(currentTag);
        }
        return tags;
    }

    private Image generateRandomImage(Tag currentTag) {
        List<Image> images = currentTag.getImages().stream().collect(Collectors.toList());
        int randomNumber = RandomNumber.getRandomNumber(images.size());
        return images.get(randomNumber);
    }


}