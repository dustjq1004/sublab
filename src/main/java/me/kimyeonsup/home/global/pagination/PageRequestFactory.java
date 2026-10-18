package me.kimyeonsup.home.global.pagination;

import lombok.Builder;
import me.kimyeonsup.home.util.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

public class PageRequestFactory {
    private int pageNumber = 1;
    private int pageSize = 10;
    private String sortBy = "createdAt";
    private Direction direction = Direction.DESC;

    @Builder
    public PageRequestFactory(int pageNumber, int pageSize, String sortBy, String direction) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;

        if (!StringUtils.isNull(sortBy))
            this.sortBy = sortBy;

        if (direction.equalsIgnoreCase("ASC")) {
            this.direction = Direction.ASC;
        }
    }

    public PageRequest createPageRequest() {
        return PageRequest.of(pageNumber - 1, pageSize, direction, sortBy);
    }
}
