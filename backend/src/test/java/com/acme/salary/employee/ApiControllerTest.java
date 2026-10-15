package com.acme.salary.employee;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
class ApiControllerTest {

    private static final String BODY = """
            {"firstName":"Ada","lastName":"Lovelace","email":"ada@x.com","country":"US","department":"Engineering",
             "level":"L3","jobTitle":"Engineer","gender":"F","hireDate":"2020-01-15","salary":100000,"currency":"USD"}""";

    @Autowired
    MockMvc mvc;

    private int create() throws Exception {
        MvcResult r = mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/employees/")))
                .andExpect(jsonPath("$.salaryUsd").value(100000.0))
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(r.getResponse().getContentAsString(), "$.id");
    }

    @Test
    void createGetUpdateDeleteLifecycle() throws Exception {
        int id = create();
        mvc.perform(get("/api/employees/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("ada@x.com"));
        mvc.perform(put("/api/employees/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("Lovelace", "Byron")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.lastName").value("Byron"));
        mvc.perform(delete("/api/employees/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/employees/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void validationFailureIsProblemJsonWithFieldErrors() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("\"ada@x.com\"", "\"bad\"").replace("100000", "-1")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.salary").exists());
    }

    @Test
    void notFoundIsProblemJson() throws Exception {
        mvc.perform(get("/api/employees/424242"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists());
        mvc.perform(get("/api/nope")).andExpect(status().isNotFound());
    }

    @Test
    void malformedJsonAndBadIdAreBadRequests() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/api/employees/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void salaryChangeAndHistory() throws Exception {
        int id = create();
        mvc.perform(post("/api/employees/" + id + "/salary").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newSalary\":120000,\"reason\":\"Annual review\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.salary").value(120000.0));
        mvc.perform(post("/api/employees/" + id + "/salary").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newSalary\":0,\"reason\":\"x\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.newSalary").exists())
                .andExpect(jsonPath("$.errors.reason").exists());
        mvc.perform(post("/api/employees/999999/salary").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newSalary\":1,\"reason\":\"valid\"}")).andExpect(status().isNotFound());
        mvc.perform(get("/api/employees/" + id + "/salary-history"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].oldSalary").value(100000.0))
                .andExpect(jsonPath("$[1].oldSalary").value((Object) null))
                .andExpect(jsonPath("$[0].changedBy").value("hr.manager"));
    }

    @Test
    void searchEndpointShapeAndLimits() throws Exception {
        create();
        mvc.perform(get("/api/employees?size=5&sort=salaryUsd,desc&q=ada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(5));
        mvc.perform(get("/api/employees?size=500")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.size").exists());
        mvc.perform(get("/api/employees?sort=hack,asc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sort").exists());
    }

    @Test
    void lookups() throws Exception {
        mvc.perform(get("/api/lookups")).andExpect(status().isOk())
                .andExpect(jsonPath("$.countries", hasSize(10)))
                .andExpect(jsonPath("$.departments", hasSize(8)))
                .andExpect(jsonPath("$.levels[0]").value("L1"))
                .andExpect(jsonPath("$.currencies").isArray());
    }

    @Test
    void importEndpointAndBadHeader() throws Exception {
        String csv = "firstName,lastName,email,country,department,level,jobTitle,gender,hireDate,salary,currency\n"
                + "A,B,ab@x.com,US,Sales,L1,AE,M,2023-01-01,50000,USD\nA,B,bad,US,Sales,L1,AE,M,2023-01-01,50000,USD\n";
        mvc.perform(multipart("/api/import/employees").file(new MockMultipartFile("file", "e.csv", "text/csv", csv.getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(2)).andExpect(jsonPath("$.imported").value(1))
                .andExpect(jsonPath("$.failed").value(1)).andExpect(jsonPath("$.errors[0].row").value(3));
        mvc.perform(multipart("/api/import/employees").file(new MockMultipartFile("file", "e.csv", "text/csv", "a,b\n1,2\n".getBytes())))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.header").exists());
        mvc.perform(multipart("/api/import/employees")).andExpect(status().isBadRequest());
    }

    @Test
    void exportHeaders() throws Exception {
        MvcResult r = mvc.perform(get("/api/export/employees?country=US")).andExpect(request().asyncStarted()).andReturn();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(r))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("employees.csv")));
    }

    private static org.springframework.test.web.servlet.result.RequestResultMatchers request() {
        return org.springframework.test.web.servlet.result.MockMvcResultMatchers.request();
    }
}
