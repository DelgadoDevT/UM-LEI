<template>
  <div ref="chartContainer" class="w-full h-full"></div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import * as d3 from 'd3';

const props = defineProps({
  data: { type: Array, required: true },
  width: { type: Number, default: 550 },
  height: { type: Number, default: 280 },
  colors: { type: Array, default: () => ['#16a34a', '#3b82f6'] }
});

const chartContainer = ref(null);

const drawChart = () => {
  if (!chartContainer.value || !props.data || props.data.length === 0) return;

  d3.select(chartContainer.value).selectAll('*').remove();

  const sanitizedSeries = props.data
    .filter((series) => series && Array.isArray(series.values))
    .map((series) => ({
      ...series,
      values: series.values
        .filter((point) => point && point.x !== null && point.x !== undefined)
        .map((point) => ({
          x: String(point.x),
          y: Number.isFinite(Number(point.y)) ? Number(point.y) : 0
        }))
    }))
    .filter((series) => series.values.length > 0);

  if (sanitizedSeries.length === 0) return;

  const margin = { top: 20, right: 30, bottom: 40, left: 50 };
  const width = props.width - margin.left - margin.right;
  const height = props.height - margin.top - margin.bottom;

  const svg = d3.select(chartContainer.value)
    .append('svg')
      .attr('width', props.width)
      .attr('height', props.height)
      .style('overflow', 'visible')
    .append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

  const xScale = d3.scalePoint()
    .domain(sanitizedSeries[0].values.map(d => d.x))
    .range([0, width])
    .padding(0.3);

  const maxY = d3.max(sanitizedSeries, series => d3.max(series.values, d => d.y)) || 10;
  const yScale = d3.scaleLinear()
    .domain([0, maxY])
    .range([height, 0])
    .nice();

  const computedYTicks = yScale.ticks(5);

  // --- GRIDLINES ---
  svg.append('g').attr('class', 'grid')
    .call(d3.axisLeft(yScale).tickValues(computedYTicks).tickSize(-width).tickFormat(''))
    .selectAll('line').style('stroke', '#e5e7eb');

  svg.append('g').attr('class', 'grid')
    .attr('transform', `translate(0, ${height})`)
    .call(d3.axisBottom(xScale).tickSize(-height).tickFormat(''))
    .selectAll('line').style('stroke', '#e5e7eb');

  svg.selectAll('.grid .domain').remove();

  // --- AXES ---
  const yAxis = svg.append('g').call(d3.axisLeft(yScale).tickValues(computedYTicks).tickFormat(d3.format('d')).tickSize(0));
  yAxis.selectAll('.domain').remove();
  yAxis.selectAll('text').style('font-size', '12px').style('fill', '#6b7280');

  const xAxis = svg.append('g').attr('transform', `translate(0,${height})`).call(d3.axisBottom(xScale));
  xAxis.selectAll('.domain, .tick line').style('stroke', '#d1d5db');
  xAxis.selectAll('text').style('font-size', '12px').style('fill', '#6b7280');

  // --- LINES AND POINTS ---
  sanitizedSeries.forEach((series, i) => {
    const line = d3.line().x(d => xScale(d.x)).y(d => yScale(d.y)).curve(d3.curveLinear);
    svg.append('path').datum(series.values).attr('d', line)
      .attr('style', `fill: none; stroke: ${props.colors[i]}; stroke-width: 2.5;`);

    svg.selectAll(`.dot-${i}`).data(series.values).enter().append('circle')
      .attr('cx', d => xScale(d.x)).attr('cy', d => yScale(d.y)).attr('r', 4)
      .attr('fill', props.colors[i]).attr('stroke', 'white').attr('stroke-width', 1.5);
  });

  // --- TOOLTIP ---
  const tooltip = d3.select(chartContainer.value).append('div')
    .attr('class', 'tooltip').style('position', 'absolute').style('visibility', 'hidden')
    .style('background', 'white').style('border', '1px solid #e2e8f0').style('border-radius', '8px')
    .style('padding', '8px 12px').style('font-size', '12px').style('pointer-events', 'none');

  const verticalLine = svg.append('line').attr('y1', 0).attr('y2', height)
    .style('stroke', '#9ca3af').style('stroke-width', 1).style('opacity', 0);

  svg.append('rect').attr('width', width).attr('height', height).attr('fill', 'none').attr('pointer-events', 'all')
    .on('mouseout', () => {
      tooltip.style('visibility', 'hidden');
      verticalLine.style('opacity', 0);
    })
    .on('mouseover', () => {
      tooltip.style('visibility', 'visible');
      verticalLine.style('opacity', 1);
    })
    .on('mousemove', (event) => {
      const [mouseX] = d3.pointer(event);
      const closestX = d3.least(xScale.domain(), d => Math.abs(xScale(d) - mouseX));

      if (closestX) {
        const posX = xScale(closestX);
        verticalLine.attr('x1', posX).attr('x2', posX);

        let tooltipHtml = `<div class="font-bold mb-1">${closestX}</div>`;
        sanitizedSeries.forEach((series, i) => {
          const point = series.values.find(v => v.x === closestX);
          if (point) {
            tooltipHtml += `<div style="display: flex; align-items: center; gap: 6px;"><div style="width: 8px; height: 8px; border-radius: 99px; background-color: ${props.colors[i]};"></div><span>${series.title}: <strong>${d3.format(',')(point.y)}</strong></span></div>`;
          }
        });
        tooltip.html(tooltipHtml)
          .style('left', (event.pageX + 15) + 'px')
          .style('top', (event.pageY - 15) + 'px');
      }
    });
};

onMounted(drawChart);
watch(() => [props.data, props.width, props.height], drawChart, { deep: true });
</script>
