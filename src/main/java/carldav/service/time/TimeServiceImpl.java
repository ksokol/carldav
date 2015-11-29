package carldav.service.time;

import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Kamill Sokol
 */
@Service("timeService")
public class TimeServiceImpl implements TimeService {

    @Override
    public Date getCurrentTime() {
        return new Date(System.currentTimeMillis());
    }
}
