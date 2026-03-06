package bms.player.beatoraja;

import bms.player.beatoraja.song.SongData;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Holding all course data on disk
 */
public class CourseDataResource {
	private final List<CourseData> courseDataList;
	// Sha256 (sorted, concat) -> courseData
	private final Map<String, CourseData> sha256CourseDataMap;
	// Md5 (sorted, concat) -> courseData
	private final Map<String, CourseData> md5CourseDataMap;

	public CourseDataResource(List<CourseData> courseDataList) {
		this.courseDataList = courseDataList;
		this.sha256CourseDataMap = courseDataList.stream()
				.filter(
						cd -> Arrays.stream(cd.getSong())
								.noneMatch(sd -> sd.getSha256() == null || sd.getSha256().isEmpty())
				)
				.collect(Collectors.toMap(
						courseData -> {
							SongData[] songs = courseData.getSong();
							return Arrays.stream(songs)
									.map(SongData::getSha256)
									.sorted()
									.collect(Collectors.joining());
						}, Function.identity(), (a, b) -> a
				));
		this.md5CourseDataMap = courseDataList.stream()
				.filter(
						cd -> Arrays.stream(cd.getSong())
								.noneMatch(sd -> sd.getMd5() == null || sd.getMd5().isEmpty())
				)
				.collect(Collectors.toMap(
				courseData -> {
					SongData[] songs = courseData.getSong();
					return Arrays.stream(songs)
							.map(SongData::getMd5)
							.sorted()
							.collect(Collectors.joining());
				}, Function.identity(), (a, b) -> a
		));
	}

	public List<CourseData> getCourseDataList() {
		return courseDataList;
	}

	public Optional<CourseData> getCourseDataBySha256(List<String> sha256List) {
		String sortedSha256s = sha256List.stream().sorted().collect(Collectors.joining());
		return Optional.ofNullable(sha256CourseDataMap.get(sortedSha256s));
	}

	public Optional<CourseData> getCourseDataByMd5(List<String> md5List) {
		String sortedMd5s = md5List.stream().sorted().collect(Collectors.joining());
		return Optional.ofNullable(md5CourseDataMap.get(sortedMd5s));
	}
}
