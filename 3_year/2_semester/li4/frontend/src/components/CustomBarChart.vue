<template>
  <div ref="chart" class="relative"></div>
</template>

<script setup>
import { onMounted, ref, watchEffect } from "vue";
import * as d3 from "d3";

const props = defineProps({
  data: Array,
  height: Number,
  width: Number,
  animations: {
    type: Boolean,
    default: true,
  },
  palette: Array,
  gridlines: {
    type: Boolean,
    default: true,
  },
  tooltip: {
    type: Boolean,
    default: true,
  }
});

const chart = ref(null);

const renderChart = () => {
  if (!props.data || props.data.length === 0) return;

  d3.select(chart.value).select("svg").remove();
  d3.select(chart.value).select(".tooltip").remove();

  const margin = { top: 20, right: 20, bottom: 30, left: 40 };
  const width = props.width - margin.left - margin.right;
  const height = props.height - margin.top - margin.bottom;

  const svg = d3
    .select(chart.value)
    .append("svg")
    .attr("width", props.width)
    .attr("height", props.height)
    .append("g")
    .attr("transform", `translate(${margin.left},${margin.top})`);

  const allData = props.data.flatMap(d => d.data);

  const x = d3.scaleBand()
    .range([0, width])
    .domain(allData.map(d => d.x))
    .padding(0.2);

  const maxY = d3.max(allData, d => d.y) || 10;
  const y = d3.scaleLinear()
    .range([height, 0])
    .domain([0, maxY])
    .nice();

  const computedYTicks = y.ticks(5);

  const xAxis = svg.append("g")
    .attr("transform", `translate(0,${height})`)
    .call(d3.axisBottom(x));

  const yAxisComp = d3.axisLeft(y).tickValues(computedYTicks);

  const yAxis = svg.append("g")
    .call(yAxisComp);

  xAxis.selectAll("text").attr("class", "fill-gray-600");
  yAxis.selectAll("text").attr("class", "fill-gray-600");
  xAxis.selectAll("path, line").attr("stroke", "#9ca3af");
  yAxis.selectAll("path, line").attr("stroke", "#9ca3af");

  // Garantir linha horizontal clara para o eixo X
  svg.append("line")
    .attr("x1", 0)
    .attr("x2", width)
    .attr("y1", height)
    .attr("y2", height)
    .attr("stroke", "#9ca3af")
    .attr("stroke-width", "1px");

  // Garantir linha vertical clara para o eixo Y
  svg.append("line")
    .attr("x1", 0)
    .attr("x2", 0)
    .attr("y1", 0)
    .attr("y2", height)
    .attr("stroke", "#9ca3af")
    .attr("stroke-width", "1px");

  if (props.gridlines) {
    const gridAxis = d3.axisLeft(y).tickSize(-width).tickFormat("").tickValues(computedYTicks);
    svg.append("g")
      .attr("class", "grid")
      .call(gridAxis)
      .selectAll("line")
      .attr("stroke", "#9ca3af")
      .attr("stroke-dasharray", null);
  }

  const tooltip = d3.select(chart.value)
    .append("div")
    .attr("class", "tooltip")
    .style("opacity", 0)
    .style("position", "absolute")
    .style("background-color", "rgba(0, 0, 0, 0.8)")
    .style("color", "white")
    .style("border-radius", "6px")
    .style("padding", "8px 12px")
    .style("font-size", "12px")
    .style("pointer-events", "none")
    .style("z-index", "10");

  const bars = svg.selectAll(".bar")
    .data(allData)
    .enter()
    .append("rect")
    .attr("class", "bar")
    .attr("x", d => x(d.x))
    .attr("width", x.bandwidth())
    .attr("fill", props.palette[0]);

  if (props.animations) {
    bars.attr("y", d => y(0))
      .attr("height", 0)
      .transition()
      .duration(800)
      .attr("y", d => y(d.y))
      .attr("height", d => height - y(d.y));
  } else {
    bars.attr("y", d => y(d.y))
      .attr("height", d => height - y(d.y));
  }

  if (props.tooltip) {
    bars
      .on("mouseover", function (event, d) {
        d3.select(this).style("opacity", 0.7);
        tooltip.transition().duration(200).style("opacity", 1);
        tooltip.html(`Clientes: ${d.y}`)
          .style("left", (event.pageX - chart.value.getBoundingClientRect().left + 15) + "px")
          .style("top", (event.pageY - chart.value.getBoundingClientRect().top - 40) + "px");
      })
      .on("mouseout", function () {
        d3.select(this).style("opacity", 1);
        tooltip.transition().duration(500).style("opacity", 0);
      });
  }
};

onMounted(renderChart);
watchEffect(renderChart);
</script>

<style>
.grid line {
  stroke: #9ca3af;
}
.grid path {
  stroke-width: 0;
}
</style>
