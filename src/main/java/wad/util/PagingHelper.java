package wad.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;


import java.util.ArrayList;
import java.util.List;

/**
 * User: Niko
 * Date: 8.12.2014
 */
public class PagingHelper<T> {

    private static final Logger logger = LoggerFactory.getLogger(PagingHelper.class);

    public static final Integer SPAN = 3;

    private Integer totalPages;
    private Integer firstPage;
    private Integer lastPage;
    private Integer currentPage;
    private Boolean isFirstPage;
    private Boolean isLastPage;

    private Boolean hasContent;

    private List<Integer> prevPages = new ArrayList<>();
    private List<Integer> nextPages = new ArrayList<>();

    public PagingHelper(Page<T> page) {

        // Extract needed inforation
        this.totalPages = page.getTotalPages();
        this.firstPage = 0;
        this.currentPage = page.getNumber();
        this.lastPage = totalPages-1;
        this.isFirstPage = (currentPage == firstPage);
        this.isLastPage = (currentPage == lastPage);
        this.hasContent = page.hasContent();

        // Count values for viewing paging navigation
        int min = currentPage - SPAN;
        int max = currentPage + SPAN;

        if(min < firstPage) {
            max = max + (SPAN % min);
            min = firstPage;
        }

        if(max > lastPage) {
            max = lastPage;
        }

        for(int i = min; i < currentPage; i++) {
            prevPages.add(i);
        }

        for(int i = currentPage +1; i <= max; i++) {
            nextPages.add(i);
        }
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getFirstPage() {
        return firstPage;
    }

    public Integer getLastPage() {
        return lastPage;
    }

    public List<Integer> getPrevPages() {
        return prevPages;
    }

    public List<Integer> getNextPages() {
        return nextPages;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public Boolean isFirstPage() {
        return isFirstPage;
    }

    public Boolean isLastPage() {
        return isLastPage;
    }

    public Boolean hasContent() {
        return hasContent;
    }
}
