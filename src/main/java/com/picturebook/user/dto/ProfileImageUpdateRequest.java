package com.picturebook.user.dto;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ProfileImageUpdateRequest{
    @Schema(description = "MinIO IMAGE URL")  
    private String profileImage;
} 

