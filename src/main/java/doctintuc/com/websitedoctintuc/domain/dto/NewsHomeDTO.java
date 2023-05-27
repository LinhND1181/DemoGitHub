package doctintuc.com.websitedoctintuc.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewsHomeDTO {
    private int id;
    private String title;
    private String description;
    private String thumbnail;
}
