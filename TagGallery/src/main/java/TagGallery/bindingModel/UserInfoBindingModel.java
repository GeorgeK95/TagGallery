package TagGallery.bindingModel;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

public class UserInfoBindingModel {

    @NotNull
    private String town;
    @NotNull
    private String country;
    @NotNull
    private String telephoneNumber;
    @NotNull
    private String information;
    @NotNull
    private MultipartFile profilePicture;
    @NotNull
    private String admin;

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getTown() {
        return this.town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTelephoneNumber() {
        return this.telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getInformation() {
        return this.information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public MultipartFile getProfilePicture() {
        return this.profilePicture;
    }

    public void setProfilePicture(MultipartFile profilePicture) {
        this.profilePicture = profilePicture;
    }
}
