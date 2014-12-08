package wad.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


/**
 * User: Niko
 * Date: 8.12.2014
 */
@RunWith(MockitoJUnitRunner.class)
public class PagingHelperTest {

    private static final int FEW_PAGES_COUNT = 4;
    private static final int FEW_PAGES_LAST_PAGE = FEW_PAGES_COUNT-1;
    private static final int MANY_PAGES_COUNT = 100;
    private static final int MANY_PAGES_LAST_PAGE = MANY_PAGES_COUNT-1;

    @Mock
    private Page<?> page;

    @Test
    public void testPageInTheBeginningFewPages() {
        when(page.getTotalPages()).thenReturn(FEW_PAGES_COUNT);
        when(page.getNumber()).thenReturn(0);

        PagingHelper<?> helper = new PagingHelper<>(page);

        assertTrue(helper.getPrevPages().isEmpty());
        assertEquals(Integer.valueOf(FEW_PAGES_LAST_PAGE), helper.getNextPages().get(helper.getNextPages().size() - 1));
    }

    @Test
    public void testPageInTheMiddleFewPages() {
        when(page.getTotalPages()).thenReturn(FEW_PAGES_COUNT);
        final int midpoint = (FEW_PAGES_COUNT/2);
        when(page.getNumber()).thenReturn(midpoint);

        PagingHelper<?> helper = new PagingHelper<>(page);

        assertEquals(Integer.valueOf(0), helper.getPrevPages().get(0));
        assertEquals(Integer.valueOf(FEW_PAGES_LAST_PAGE), helper.getNextPages().get(helper.getNextPages().size()-1));
    }

    @Test
    public void testPageInTheEndFewPages() {
        when(page.getTotalPages()).thenReturn(FEW_PAGES_COUNT);
        when(page.getNumber()).thenReturn(FEW_PAGES_LAST_PAGE);

        PagingHelper<?> helper = new PagingHelper<>(page);

        assertEquals(Integer.valueOf(0), helper.getPrevPages().get(0));
        assertTrue(helper.getNextPages().isEmpty());
    }


    @Test
    public void testPageInTheBeginning() {
        when(page.getTotalPages()).thenReturn(MANY_PAGES_COUNT);
        when(page.getNumber()).thenReturn(0);

        PagingHelper<?> helper = new PagingHelper<>(page);

        assertTrue(helper.getPrevPages().isEmpty());
        assertEquals(Integer.valueOf(3), helper.getNextPages().get(helper.getNextPages().size()-1));
    }

    @Test
    public void testPageInTheMiddle() {
        when(page.getTotalPages()).thenReturn(MANY_PAGES_COUNT);
        final int midpoint = (MANY_PAGES_COUNT/2);
        when(page.getNumber()).thenReturn(midpoint);

        PagingHelper<?> helper = new PagingHelper<>(page);

        assertEquals(Integer.valueOf((midpoint-PagingHelper.SPAN)), helper.getPrevPages().get(0));
        assertEquals(Integer.valueOf((midpoint+PagingHelper.SPAN)), helper.getNextPages().get(helper.getNextPages().size()-1));
    }

    @Test
    public void testPageInTheEnd() {
        when(page.getTotalPages()).thenReturn(MANY_PAGES_COUNT);
        when(page.getNumber()).thenReturn(MANY_PAGES_LAST_PAGE);

        PagingHelper<?> helper = new PagingHelper<>(page);

        assertEquals(Integer.valueOf((MANY_PAGES_LAST_PAGE-PagingHelper.SPAN)), helper.getPrevPages().get(0));
        assertTrue(helper.getNextPages().isEmpty());
    }
}
