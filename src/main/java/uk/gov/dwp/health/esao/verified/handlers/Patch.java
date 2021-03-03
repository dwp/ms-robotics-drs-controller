package uk.gov.dwp.health.esao.verified.handlers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.dwp.health.esao.casedata.models.Tag;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Patch {

  private String op = "add";

  private String path = "/tags";

  private List<Tag> value;

}
