import org.springframework.boot.test.context.SpringBootTest;
import spock.lang.Specification;

@SpringBootTest
class SampleSpec extends Specification {

    def "spec title"() {
        when: "spec condition"
        then:
        1 + 1 == 2
    }
}
