package com.mgiglione.service.test.unit;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.mgiglione.controller.MangaController;
import com.mgiglione.model.Manga;
import com.mgiglione.service.MangaService;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MangaControllerUnitTest
{

    MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    MangaController mangaController;

    @MockBean
    MangaService mangaService;
    
    /**
     * List of samples mangas
     */
    private List<Manga> mangas;

    private static final Logger logger = LoggerFactory.getLogger(MangaControllerUnitTest.class);

    @Before
    public void setup() throws Exception
    {
        logger.info("[[ CI_CD_Tool ]] setup() in");

        //??????mvc mock????????????controller?????????????????????????????????
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.mangaController).build();//??????????????????????????????
        //this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build(); //?????????????????????web????????????

        //????????????MANGA??????
        Manga manga1 = Manga.builder()
                            .title("Hokuto no ken")
                            .synopsis("The year is 199X. The Earth has been devastated by nuclear war...")
                            .build();

        Manga manga2 = Manga.builder()
                            .title("Yumekui Kenbun")
                            .synopsis("For those who suffer nightmares, help awaits at the Ginseikan Tea House, "
                                    + "where patrons can order much more than just Darjeeling. "
                                    + "Hiruko is a special kind of a private investigator. He's a dream eater....")
                            .build();

        mangas = new ArrayList<>();
        mangas.add(manga1);
        mangas.add(manga2);

        logger.info("[[ CI_CD_Tool ]] setup() out");

    }

    @Test
    public void testSearchSync() throws Exception
    {
        logger.info("[[ CI_CD_Tool ]] testSearchSync() in");

        //???????????????????????????Controller.getMangasByTitle()???????????????????????????????????????MangaService???????????????????????????mock???????????????????????????????????????????????????
        when(mangaService.getMangasByTitle(any(String.class))).thenReturn(mangas);

        //??????????????????MangaController???SearchSync()????????????????????????????????????????????????????????????????????????????????????????????????service?????????????????????????????????MOCK???????????????
        //?????????????????????????????????????????????MOCK?????????????????????????????????????????????????????????
        mockMvc.perform(get("/manga/sync/ken").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Hokuto no ken")))
                .andExpect(jsonPath("$[1].title", is("Yumekui Kenbun")));

        logger.info("[[ CI_CD_Tool ]] testSearchSync() out");
    }

    @Test
    public void testSearchASync() throws Exception
    {

        logger.info("[[ CI_CD_Tool ]] testSearchASync() in");

        // Mocking service ????????????????????????????????????????????????MANGA???????????????
        when(mangaService.getMangasByTitle(any(String.class))).thenReturn(mangas);

        //??????????????????????????????future??????
        MvcResult result = mockMvc.perform(get("/manga/async/ken").contentType(MediaType.APPLICATION_JSON))
                                  .andDo(print())
                                  .andExpect(request().asyncStarted())
                                  .andDo(print())
                                  // .andExpect(status().is2xxSuccessful()).andReturn();
                                  .andReturn();

        // result.getRequest().getAsyncContext().setTimeout(10000);

        mockMvc.perform(asyncDispatch(result))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Hokuto no ken")));

        logger.info("[[ CI_CD_Tool ]] testSearchASync() out");

    }
}
