
package com.example.TeamAppDemo.Controller;

import com.example.TeamAppDemo.Service.TeamService;
import com.example.TeamAppDemo.Entity.Team;
import com.example.TeamAppDemo.DTO.TeamDtos;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TeamController.class)
class TeamControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean TeamService teamService;

    private Team stubTeam(Long id, String name) {
        Team t = mock(Team.class);
        when(t.getId()).thenReturn(id);
        when(t.getName()).thenReturn(name);
        when(t.getCity()).thenReturn("City");
        when(t.getCountry()).thenReturn("Country");
        when(t.getDescription()).thenReturn("Desc");
        when(t.getLogo()).thenReturn(new byte[0]); // logoPresent=false

        // ✅ Team#getDrivers() returns List<Driver>, not Set<Driver>
        when(t.getDrivers()).thenReturn(Collections.emptyList());

        return t;
    }

    @Test
    void create_multipart_returnsTeamResponse() throws Exception {
        Team t = stubTeam(1L, "Ferrari");
        when(teamService.create(any(TeamDtos.TeamCreateRequest.class), any())).thenReturn(t);

        String reqJson = """
          {"teamName":"Ferrari","city":"Maranello","country":"Italy","description":"Scuderia Ferrari"}
        """;

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "request.json", "application/json", reqJson.getBytes()
        );
        MockMultipartFile logoPart = new MockMultipartFile(
                "logo", "logo.png", "image/png", new byte[1024]
        );

        mockMvc.perform(multipart("/api/teams")
                    .file(requestPart)
                    .file(logoPart)
                    .contentType(MediaType.MULTIPART_FORM_DATA))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.teamName").value("Ferrari"))
               .andExpect(jsonPath("$.logoPresent").value(false));

        ArgumentCaptor<TeamDtos.TeamCreateRequest> cap = ArgumentCaptor.forClass(TeamDtos.TeamCreateRequest.class);
        verify(teamService).create(cap.capture(), any());
        assert cap.getValue().teamName.equals("Ferrari");
    }

    @Test
    void update_returnsUpdatedTeam() throws Exception {
        Team t = stubTeam(3L, "Mercedes-AMG");
        when(teamService.update(eq(3L), any(TeamDtos.TeamUpdateRequest.class))).thenReturn(t);

        String json = """
          {"teamName":"Mercedes-AMG","city":"Brackley","country":"UK","description":"Updated"}
        """;

        mockMvc.perform(put("/api/teams/3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(3))
               .andExpect(jsonPath("$.teamName").value("Mercedes-AMG"));
    }

    @Test
    void updateLogo_putMultipart_invokesService() throws Exception {
        byte[] bytes = new byte[256];
        MockMultipartFile logo = new MockMultipartFile("logo", "new.png", "image/png", bytes);

        mockMvc.perform(multipart("/api/teams/5/logo")
                    .file(logo)
                    .with(req -> { req.setMethod("PUT"); return req; })
                    .contentType(MediaType.MULTIPART_FORM_DATA))
               .andExpect(status().isOk());

        verify(teamService).updateLogo(eq(5L), any(org.springframework.web.multipart.MultipartFile.class));
    }

    @Test
    void get_returnsTeamResponse() throws Exception {
        Team t = stubTeam(9L, "Williams");
        when(teamService.get(9L)).thenReturn(t);

        mockMvc.perform(get("/api/teams/9"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(9))
               .andExpect(jsonPath("$.teamName").value("Williams"));
    }

    @Test
    void list_returnsArray() throws Exception {
        Team t1 = stubTeam(1L, "Ferrari");
        Team t2 = stubTeam(2L, "Mercedes-AMG");
        when(teamService.getAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/teams"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].teamName").value("Ferrari"))
               .andExpect(jsonPath("$[1].teamName").value("Mercedes-AMG"));
    }

    @Test
    void delete_invokesServiceDelete() throws Exception {
        mockMvc.perform(delete("/api/teams/4"))
               .andExpect(status().isOk());
        verify(teamService).delete(4L);
    }
}
