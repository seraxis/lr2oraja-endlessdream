package bms.tool.mdprocessor;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest(httpPort = 11177)
public class HttpDownloadProcessorTest {
	private static String apiPrefix = "http://127.0.0.1:11177/";
	private final Logger logger = LoggerFactory.getLogger(HttpDownloadProcessorTest.class);
	private static Consumer<String> emptyHook = (dir) -> {
	};

	@TempDir
	private Path tempDir;

	/**
	 * Smoke test, check if wiremock works
	 */
	@Test
	public void smoke(WireMockRuntimeInfo wmRuntimeInfo) {
		stubFor(
				get(urlPathEqualTo("/test"))
						.willReturn(
								aResponse()
										.withStatus(200)
						)
		);

		logger.info("Running at {}", wmRuntimeInfo.getHttpPort());

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(String.format("http://127.0.0.1:%d/test", wmRuntimeInfo.getHttpPort())))
				.timeout(Duration.ofSeconds(5))
				.GET()
				.build();
		HttpResponse<String> resp = null;
		try {
			resp = client.send(req, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			fail(e);
		}
		assertNotNull(resp);
		assertEquals(200, resp.statusCode());
	}

	@Nested
	@DisplayName("Single direct download service | No switch")
	class DirectDownloadServiceSimpleTest {
		private HttpDownloadProcessor downloadProcessor;

		@BeforeEach
		void setUp() throws IOException {
			stubFor(get(urlPathEqualTo("/existed"))
					.willReturn(aResponse()
							.withStatus(200)
							.withHeader("Content-Type", "application/octet-stream")
							.withBody(loadTestArchieves("foo.7z"))));
			stubFor(get(urlPathEqualTo("/notExisted"))
					.willReturn(aResponse()
							.withStatus(404)
							.withBody("Not Found")));

			HttpDownloadSource downloadSource = new HttpDownloadSource() {
				@Override
				public String getDownloadURLBasedOnMd5(String md5) throws RuntimeException {
					return apiPrefix + md5;
				}

				@Override
				public String getName() {
					return "Direct Download";
				}

				@Override
				public boolean isAllowDownloadThroughMd5() {
					return true;
				}
			};
			Map<String, HttpDownloadSource> downloadSources = new HashMap<>();
			downloadSources.put(downloadSource.getName(), downloadSource);
			downloadProcessor = new HttpDownloadProcessor(
					emptyHook,
					tempDir.toString(),
					downloadSource,
					downloadSources,
					DummyHandler
			);
		}

		@Test
		void completeDownload() throws InterruptedException {
			downloadProcessor.submitMD5Task("existed", "task name");
			Map<Integer, DownloadTask> tasksSnapshotAfterSubmitting = downloadProcessor.getAllTasks();
			assertEquals(1, tasksSnapshotAfterSubmitting.size(), "tasks size is not 1 after submitting");
			loopUntilEveryTaskEnded(downloadProcessor);

			Collection<DownloadTask> snapshots = downloadProcessor.getAllTasks().values();
			Optional<DownloadTask> any = snapshots.stream().findAny();
			assertTrue(any.isPresent());
			assertEquals(DownloadTask.DownloadTaskStatus.Extracted, any.get().getDownloadTaskStatus(), "Task status is not extracted");
			assertTrue(Files.exists(tempDir.resolve("foo/foo.bms")));
		}

		@Test
		void fail404() throws InterruptedException {
			downloadProcessor.submitMD5Task("notExisted", "task name");
			Map<Integer, DownloadTask> tasksSnapshotAfterSubmitting = downloadProcessor.getAllTasks();
			assertEquals(1, tasksSnapshotAfterSubmitting.size(), "tasks size is not 1 after submitting");
			loopUntilEveryTaskEnded(downloadProcessor);

			Collection<DownloadTask> snapshots = downloadProcessor.getAllTasks().values();
			Optional<DownloadTask> any = snapshots.stream().findAny();
			assertTrue(any.isPresent());
			assertEquals(DownloadTask.DownloadTaskStatus.Error, any.get().getDownloadTaskStatus(), "Task status is not error");
		}
	}

	private static byte[] loadTestArchieves(String fileName) throws IOException {
		try (InputStream in = HttpDownloadProcessorTest.class.getClassLoader().getResourceAsStream(fileName)) {
			if (in == null) {
				throw new IllegalArgumentException(fileName + " not found");
			}
			return in.readAllBytes();
		}
	}

	private static HttpDownloadErrorEventHandler DummyHandler = new HttpDownloadErrorEventHandler() {
		private List<HttpDownloadErrorEvent> events = new ArrayList<>();

		@Override
		public void handle(HttpDownloadErrorEvent event) {
			events.add(event);
		}

		public List<HttpDownloadErrorEvent> getEvents() {
			return events;
		}
	};

	private static boolean isEveryTaskEnded(Collection<DownloadTask> tasks) {
		return tasks.stream().allMatch(task -> {
			DownloadTask.DownloadTaskStatus status = task.getDownloadTaskStatus();
			return status != DownloadTask.DownloadTaskStatus.Prepare && status != DownloadTask.DownloadTaskStatus.Downloading;
		});
	}

	private static void loopUntilEveryTaskEnded(HttpDownloadProcessor httpDownloadProcessor) throws InterruptedException {
		while (true) {
			Map<Integer, DownloadTask> snapshot = httpDownloadProcessor.getAllTasks();
			if (isEveryTaskEnded(snapshot.values())) {
				break;
			}
			Thread.sleep(100);
		}
	}
}
