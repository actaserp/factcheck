package mes.domain.DTO;


import lombok.Getter;
import lombok.Setter;
import mes.domain.entity.User;

@Getter
@Setter
public class UserDto {

    private Integer id;
    private String username;
    private String first_name;
    private String email;
    private Boolean superUser;
    private Boolean active;
    private String last_name;
    private Boolean is_staff;
    private String tel;
    private String agencycd;
    private String divinm;


    public static UserDto toDto(User userEntity) {
        UserDto userDto = new UserDto();
        userDto.setId(userEntity.getId());
        userDto.setUsername(userEntity.getUsername());
        userDto.setFirst_name(userEntity.getFirst_name());
        userDto.setEmail(userEntity.getEmail());
        userDto.setSuperUser(userEntity.getSuperUser());
        userDto.setActive(userEntity.getActive());
        userDto.setLast_name(userEntity.getLast_name());
        userDto.setIs_staff(userEntity.getIs_staff());
        userDto.setTel(userEntity.getTel());
        userDto.setAgencycd(userEntity.getAgencycd());
        userDto.setDivinm(userEntity.getDivinm());
        return userDto;
    }

    public static UserDto of(String username, String nickname, String email, String phone) {
        UserDto userDto = new UserDto();
        userDto.setUsername(username);
        userDto.setFirst_name(nickname);
        userDto.setEmail(email);
        userDto.setTel(phone);
        return userDto; // ✅ 새로운 객체 반환 (안전함)
    }

}
