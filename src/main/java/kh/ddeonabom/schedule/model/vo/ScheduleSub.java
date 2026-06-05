package kh.ddeonabom.schedule.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class ScheduleSub {
	private int scheduleSubNo;
	private Date scheduleSubDate;
	private int scheduleSubSeq;
	private int scheduleNo;
	private int contentId;
}
