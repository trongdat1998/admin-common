package io.bhex.bhop.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PaginationVO<T> implements Serializable {
    private Integer current;
    private Integer pageSize;
    private Integer total;

    private List<T> list;

}
