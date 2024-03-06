package task3;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Json {
    @SerializedName(value = "count")
    private int count;
    @SerializedName(value = "inverted_array")
    private List<Integer> invertedArray;
    @SerializedName(value = "word")
    private String word;
}
