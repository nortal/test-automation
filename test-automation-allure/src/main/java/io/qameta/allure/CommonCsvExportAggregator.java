/**
 * Copyright (c) 2022 Nortal AS
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.qameta.allure;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This is a copy of allure CommonCsvExportAggregator with removed opencsv customization.
 *
 * @param <T>
 */
public abstract class CommonCsvExportAggregator<T> implements Aggregator {

    private final String fileName;

    private final Class<T> type;

    public CommonCsvExportAggregator(final String fileName, final Class<T> type) {
        this.fileName = fileName;
        this.type = type;
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        final Path dataFolder = Files.createDirectories(outputDirectory.resolve(Constants.DATA_DIR));
        final Path csv = dataFolder.resolve(fileName);

        try (Writer writer = Files.newBufferedWriter(csv)) {
            final StatefulBeanToCsvBuilder<T> builder = new StatefulBeanToCsvBuilder<>(writer);
            final ColumnPositionMappingStrategy<T> mappingStrategy = new ColumnPositionMappingStrategy<>();
            mappingStrategy.setType(type);
            final StatefulBeanToCsv<T> beanWriter = builder.withMappingStrategy(mappingStrategy).build();
            try {
                beanWriter.write(getData(launchesResults));
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    protected abstract List<T> getData(List<LaunchResults> launchesResults);

}
