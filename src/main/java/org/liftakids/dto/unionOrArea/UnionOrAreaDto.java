package org.liftakids.dto.unionOrArea;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnionOrAreaDto {
    private Long unionOrAreaId;
    private String unionOrAreaName;
    private Long thanaId;
}
